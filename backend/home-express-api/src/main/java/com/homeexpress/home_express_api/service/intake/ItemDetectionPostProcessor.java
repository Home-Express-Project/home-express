package com.homeexpress.home_express_api.service.intake;

import com.homeexpress.home_express_api.dto.intake.ItemCandidateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Post-processing service for detected items
 * - Aggregates similar items across multiple images
 * - Normalizes Vietnamese item names
 * - Maps categories to Vietnamese categories
 * - Improves quantity detection
 */
@Slf4j
@Service
public class ItemDetectionPostProcessor {

    /**
     * Process and aggregate detected items
     * - Breaks down sets into individual items
     * - Merges similar items from different images
     * - Normalizes names and categories
     * - Updates quantities
     */
    public List<ItemCandidateDto> processAndAggregate(List<ItemCandidateDto> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        log.info("Post-processing {} candidate items", candidates.size());

        // Step 1: Break down sets into individual items (e.g., "Bộ bàn ghế" → "Bàn ăn" + "Ghế ăn")
        List<ItemCandidateDto> expanded = expandSets(candidates);

        // Step 2: Normalize names and categories, calculate size
        List<ItemCandidateDto> normalized = expanded.stream()
            .map(this::normalizeItem)
            .map(this::calculateSize)
            .collect(Collectors.toList());

        // Step 3: Aggregate similar items
        List<ItemCandidateDto> aggregated = aggregateSimilarItems(normalized);

        log.info("Post-processing complete: {} items after expansion and aggregation", aggregated.size());

        return aggregated;
    }

    /**
     * Expand sets into individual items
     * Examples:
     * - "Bộ bàn ghế" (1 table + 4 chairs) → "Bàn ăn" (1) + "Ghế ăn" (4)
     * - "Dining Set" → "Dining Table" + "Dining Chairs"
     */
    private List<ItemCandidateDto> expandSets(List<ItemCandidateDto> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        List<ItemCandidateDto> expanded = new ArrayList<>();

        for (ItemCandidateDto item : items) {
            if (item == null || item.getName() == null) {
                continue;
            }

            String name = item.getName().toLowerCase();
            
            // Check if this is a set that needs to be broken down
            if (isSetItem(name)) {
                List<ItemCandidateDto> setItems = breakDownSet(item);
                expanded.addAll(setItems);
            } else {
                // Not a set, keep as is
                expanded.add(item);
            }
        }

        return expanded;
    }

    /**
     * Check if an item name represents a set
     */
    private boolean isSetItem(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }

        String lowerName = name.toLowerCase();
        return lowerName.contains("bộ bàn ghế") ||
               lowerName.contains("dining set") ||
               lowerName.contains("furniture set") ||
               lowerName.contains("sofa set") ||
               lowerName.contains("bedroom set") ||
               lowerName.contains("table set") ||
               lowerName.contains("chair set") ||
               (lowerName.contains("set") && (lowerName.contains("table") || lowerName.contains("chair") || lowerName.contains("furniture")));
    }

    /**
     * Break down a set item into individual items
     */
    private List<ItemCandidateDto> breakDownSet(ItemCandidateDto setItem) {
        List<ItemCandidateDto> items = new ArrayList<>();
        String name = setItem.getName().toLowerCase();

        // Extract metadata to pass to individual items
        Map<String, Object> baseMetadata = new HashMap<>();
        if (setItem.getMetadata() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> existingMetadata = (Map<String, Object>) setItem.getMetadata();
            baseMetadata.putAll(existingMetadata);
        }
        baseMetadata.put("fromSet", true);
        baseMetadata.put("originalSetName", setItem.getName());

        if (name.contains("bộ bàn ghế") || name.contains("dining set")) {
            // Dining set: 1 table + N chairs (typically 4-6)
            // Try to extract chair count from name or metadata, default to 4
            int chairCount = extractChairCount(setItem, 4);

            // Create dining table
            items.add(createItemFromSet(
                setItem, "Bàn ăn", "furniture", "dining_table", 1, baseMetadata));

            // Create dining chairs
            items.add(createItemFromSet(
                setItem, "Ghế ăn", "furniture", "dining_chair", chairCount, baseMetadata));

        } else if (name.contains("sofa set")) {
            // Sofa set: 1 sofa + 1 coffee table + possibly side tables
            items.add(createItemFromSet(
                setItem, "Sofa", "furniture", "sofa", 1, baseMetadata));
            items.add(createItemFromSet(
                setItem, "Bàn trà", "furniture", "coffee_table", 1, baseMetadata));
            
        } else if (name.contains("bedroom set")) {
            // Bedroom set: 1 bed + 2 nightstands + possibly dresser/wardrobe
            items.add(createItemFromSet(
                setItem, "Giường", "furniture", "bed_frame", 1, baseMetadata));
            items.add(createItemFromSet(
                setItem, "Tủ đầu giường", "furniture", "nightstand", 2, baseMetadata));
            
        } else {
            // Generic furniture set - try to infer from context
            // For now, keep as is but mark as processed
            log.warn("Unknown set type: {}. Keeping as single item.", setItem.getName());
            items.add(setItem);
        }

        return items;
    }

    /**
     * Extract chair count from item name or metadata
     */
    private int extractChairCount(ItemCandidateDto item, int defaultValue) {
        // Check metadata first
        if (item.getMetadata() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) item.getMetadata();
            Object chairCountObj = metadata.get("chairCount");
            if (chairCountObj instanceof Number) {
                return ((Number) chairCountObj).intValue();
            }
        }

        // Try to extract from name (e.g., "Bộ bàn ghế 6 chỗ" → 6)
        String name = item.getName();
        if (name != null) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*(chỗ|chair|ghế)");
            java.util.regex.Matcher matcher = pattern.matcher(name.toLowerCase());
            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }

        return defaultValue;
    }

    /**
     * Create an individual item from a set item
     */
    private ItemCandidateDto createItemFromSet(
            ItemCandidateDto setItem, 
            String itemName, 
            String category, 
            String subcategory, 
            int quantity,
            Map<String, Object> baseMetadata) {
        
        Map<String, Object> metadata = new HashMap<>(baseMetadata);
        metadata.put("subcategory", subcategory);

        return ItemCandidateDto.builder()
            .id(UUID.randomUUID().toString())
            .name(itemName)
            .categoryName(normalizeCategory(category, itemName))
            .categoryId(setItem.getCategoryId())
            .size(setItem.getSize())
            .weightKg(setItem.getWeightKg())
            .dimensions(setItem.getDimensions())
            .quantity(quantity)
            .isFragile(setItem.getIsFragile())
            .requiresDisassembly(setItem.getRequiresDisassembly())
            .requiresPackaging(setItem.getRequiresPackaging())
            .source(setItem.getSource())
            .confidence(setItem.getConfidence())
            .imageUrl(setItem.getImageUrl())
            .notes(setItem.getNotes())
            .metadata(metadata)
            .build();
    }

    /**
     * Normalize item name and category (Vietnamese support)
     */
    private ItemCandidateDto normalizeItem(ItemCandidateDto item) {
        if (item == null || item.getName() == null) {
            return item;
        }

        String originalName = item.getName();
        String normalizedName = normalizeVietnameseName(originalName);
        String normalizedCategory = normalizeCategory(item.getCategoryName(), normalizedName);

        // Update item if name/category changed
        if (!originalName.equals(normalizedName) || 
            (item.getCategoryName() != null && !item.getCategoryName().equals(normalizedCategory))) {
            
            ItemCandidateDto.ItemCandidateDtoBuilder builder = ItemCandidateDto.builder()
                .id(item.getId())
                .name(normalizedName)
                .categoryName(normalizedCategory)
                .categoryId(item.getCategoryId())
                .size(item.getSize())
                .weightKg(item.getWeightKg())
                .dimensions(item.getDimensions())
                .quantity(item.getQuantity() != null ? item.getQuantity() : 1)
                .isFragile(item.getIsFragile())
                .requiresDisassembly(item.getRequiresDisassembly())
                .requiresPackaging(item.getRequiresPackaging())
                .source(item.getSource())
                .confidence(item.getConfidence())
                .imageUrl(item.getImageUrl())
                .notes(item.getNotes());

            // Preserve metadata and add normalization info
            Map<String, Object> metadata = new HashMap<>();
            if (item.getMetadata() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> existingMetadata = (Map<String, Object>) item.getMetadata();
                metadata.putAll(existingMetadata);
            }
            metadata.put("originalName", originalName);
            metadata.put("normalized", true);
            builder.metadata(metadata);

            return builder.build();
        }

        return item;
    }

    /**
     * Normalize Vietnamese item names to standard format
     */
    private String normalizeVietnameseName(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }

        String normalized = name.trim();

        // Vietnamese to English mapping
        Map<String, String> vietnameseMap = Map.ofEntries(
            Map.entry("tủ lạnh", "Tủ lạnh"),
            Map.entry("tủ lạnh samsung", "Tủ lạnh Samsung"),
            Map.entry("tủ lạnh lg", "Tủ lạnh LG"),
            Map.entry("máy giặt", "Máy giặt"),
            Map.entry("máy sấy", "Máy sấy"),
            Map.entry("tivi", "Tivi"),
            Map.entry("tv", "Tivi"),
            Map.entry("sofa", "Sofa"),
            Map.entry("ghế sofa", "Sofa"),
            Map.entry("bàn ăn", "Bàn ăn"),
            Map.entry("bàn làm việc", "Bàn làm việc"),
            Map.entry("giường", "Giường"),
            Map.entry("tủ quần áo", "Tủ quần áo"),
            Map.entry("tủ sách", "Tủ sách"),
            Map.entry("ghế văn phòng", "Ghế văn phòng"),
            Map.entry("điều hòa", "Điều hòa"),
            Map.entry("lò vi sóng", "Lò vi sóng"),
            Map.entry("máy rửa chén", "Máy rửa chén"),
            Map.entry("thùng carton", "Thùng carton"),
            Map.entry("bộ bàn ghế", "Bộ bàn ghế")
        );

        String lowerName = normalized.toLowerCase();
        for (Map.Entry<String, String> entry : vietnameseMap.entrySet()) {
            if (lowerName.contains(entry.getKey())) {
                // Preserve brand/model if present
                String brandModel = extractBrandModel(normalized);
                if (brandModel != null && !brandModel.isEmpty()) {
                    return entry.getValue() + " " + brandModel;
                }
                return entry.getValue();
            }
        }

        // Capitalize first letter if all lowercase
        if (normalized.equals(normalized.toLowerCase()) && normalized.length() > 0) {
            return normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
        }

        return normalized;
    }

    /**
     * Extract brand/model from name
     */
    private String extractBrandModel(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        // Common brand patterns
        String[] brands = {"Samsung", "LG", "Sony", "Panasonic", "TCL", "Sharp", 
                          "Toshiba", "IKEA", "Xiaomi", "Electrolux", "Bosch", "Whirlpool"};
        
        for (String brand : brands) {
            if (name.toLowerCase().contains(brand.toLowerCase())) {
                return brand;
            }
        }

        // Check for model numbers (e.g., "RT35K", "UN55TU7000")
        String[] parts = name.split("\\s+");
        for (String part : parts) {
            if (part.matches(".*\\d+.*") && part.length() >= 3) {
                return part;
            }
        }

        return null;
    }

    /**
     * Normalize category to Vietnamese category system
     */
    private String normalizeCategory(String category, String itemName) {
        if (category == null) {
            category = inferCategoryFromName(itemName);
        }

        // Map English categories to Vietnamese categories used in the system
        Map<String, String> categoryMap = Map.of(
            "furniture", "Nội thất",
            "appliance", "Điện tử",
            "electronics", "Điện tử",
            "box", "Khác",
            "other", "Khác"
        );

        String normalized = categoryMap.get(category != null ? category.toLowerCase() : null);
        return normalized != null ? normalized : (category != null ? category : "Khác");
    }

    /**
     * Infer category from item name
     */
    private String inferCategoryFromName(String name) {
        if (name == null || name.isBlank()) {
            return "other";
        }

        String lowerName = name.toLowerCase();

        // Furniture keywords
        if (lowerName.matches(".*(sofa|bàn|ghế|giường|tủ|kệ|tủ quần áo|tủ sách|bộ bàn ghế).*")) {
            return "furniture";
        }

        // Appliance keywords
        if (lowerName.matches(".*(tủ lạnh|máy giặt|máy sấy|điều hòa|lò vi sóng|máy rửa chén).*")) {
            return "appliance";
        }

        // Electronics keywords
        if (lowerName.matches(".*(tivi|tv|máy tính|laptop|monitor|màn hình|printer|loa|speaker).*")) {
            return "electronics";
        }

        // Box keywords
        if (lowerName.matches(".*(thùng|box|carton|container|crate).*")) {
            return "box";
        }

        return "other";
    }

    /**
     * Aggregate similar items (same name and category) from different images
     */
    private List<ItemCandidateDto> aggregateSimilarItems(List<ItemCandidateDto> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        // Group by normalized name and category
        Map<String, List<ItemCandidateDto>> grouped = items.stream()
            .collect(Collectors.groupingBy(item -> {
                String name = item.getName() != null ? item.getName().toLowerCase().trim() : "";
                String category = item.getCategoryName() != null ? item.getCategoryName() : "";
                return name + "|" + category;
            }));

        List<ItemCandidateDto> aggregated = new ArrayList<>();

        for (List<ItemCandidateDto> group : grouped.values()) {
            if (group.size() == 1) {
                // Single item, no aggregation needed
                aggregated.add(group.get(0));
            } else {
                // Aggregate multiple similar items
                ItemCandidateDto aggregatedItem = aggregateItemGroup(group);
                aggregated.add(aggregatedItem);
            }
        }

        return aggregated;
    }

    /**
     * Aggregate a group of similar items into one
     */
    private ItemCandidateDto aggregateItemGroup(List<ItemCandidateDto> group) {
        if (group == null || group.isEmpty()) {
            return null;
        }

        if (group.size() == 1) {
            return group.get(0);
        }

        // Use the first item as base
        ItemCandidateDto base = group.get(0);

        // Sum quantities
        int totalQuantity = group.stream()
            .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 1)
            .sum();

        // Average confidence
        double avgConfidence = group.stream()
            .filter(item -> item.getConfidence() != null)
            .mapToDouble(ItemCandidateDto::getConfidence)
            .average()
            .orElse(base.getConfidence() != null ? base.getConfidence() : 0.8);

        // Use highest weight (if available)
        Double maxWeight = group.stream()
            .map(ItemCandidateDto::getWeightKg)
            .filter(Objects::nonNull)
            .max(Comparator.comparingDouble(weight -> weight != null ? weight : 0.0))
            .orElse(null);

        // Use largest dimensions (if available)
        ItemCandidateDto.DimensionsDto maxDims = group.stream()
            .map(ItemCandidateDto::getDimensions)
            .filter(Objects::nonNull)
            .max(Comparator.comparing(d -> {
                if (d == null) return 0.0;
                double width = d.getWidthCm() != null ? d.getWidthCm() : 0.0;
                double height = d.getHeightCm() != null ? d.getHeightCm() : 0.0;
                double depth = d.getDepthCm() != null ? d.getDepthCm() : 0.0;
                return width * height * depth;
            }))
            .orElse(null);

        // Combine metadata
        Map<String, Object> combinedMetadata = new HashMap<>();
        if (base.getMetadata() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> baseMetadata = (Map<String, Object>) base.getMetadata();
            combinedMetadata.putAll(baseMetadata);
        }
        combinedMetadata.put("aggregatedFrom", group.size());
        combinedMetadata.put("sourceImages", group.stream()
            .map(item -> {
                if (item.getMetadata() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> meta = (Map<String, Object>) item.getMetadata();
                    Object imageIndex = meta.get("imageIndex");
                    return imageIndex != null ? imageIndex.toString() : "unknown";
                }
                return "unknown";
            })
            .collect(Collectors.toList()));

        return ItemCandidateDto.builder()
            .id(base.getId())
            .name(base.getName())
            .categoryName(base.getCategoryName())
            .categoryId(base.getCategoryId())
            .size(base.getSize())
            .weightKg(maxWeight != null ? maxWeight : base.getWeightKg())
            .dimensions(maxDims != null ? maxDims : base.getDimensions())
            .quantity(totalQuantity)
            .isFragile(base.getIsFragile())
            .requiresDisassembly(base.getRequiresDisassembly())
            .requiresPackaging(base.getRequiresPackaging())
            .source(base.getSource())
            .confidence(avgConfidence)
            .imageUrl(base.getImageUrl())
            .notes(base.getNotes())
            .metadata(combinedMetadata)
            .build();
    }

    /**
     * Calculate size (S, M, L) based on weight and dimensions
     * Rules from frontend:
     * - S: weight < 20kg or volume < 0.25 m³
     * - M: weight 20-50kg or volume 0.25-0.85 m³
     * - L: weight > 50kg or volume > 0.85 m³
     */
    private ItemCandidateDto calculateSize(ItemCandidateDto item) {
        if (item == null) {
            return item;
        }

        // If size already set, keep it
        if (item.getSize() != null && !item.getSize().isBlank()) {
            return item;
        }

        String calculatedSize = "M"; // Default

        // Calculate volume in m³
        Double volumeM3 = null;
        if (item.getDimensions() != null) {
            Double width = item.getDimensions().getWidthCm();
            Double height = item.getDimensions().getHeightCm();
            Double depth = item.getDimensions().getDepthCm();
            
            if (width != null && height != null && depth != null) {
                volumeM3 = (width * height * depth) / 1_000_000.0; // Convert cm³ to m³
            }
        }

        // Determine size based on weight first, then volume
        Double weightKg = item.getWeightKg();
        if (weightKg != null) {
            if (weightKg < 20) {
                calculatedSize = "S";
            } else if (weightKg <= 50) {
                calculatedSize = "M";
            } else {
                calculatedSize = "L";
            }
        } else if (volumeM3 != null) {
            // Use volume if weight not available
            if (volumeM3 < 0.25) {
                calculatedSize = "S";
            } else if (volumeM3 <= 0.85) {
                calculatedSize = "M";
            } else {
                calculatedSize = "L";
            }
        } else {
            // If neither weight nor dimensions available, infer from category
            calculatedSize = inferSizeFromCategory(item.getCategoryName(), item.getName());
        }

        // Update item with calculated size
        if (!calculatedSize.equals(item.getSize())) {
            return ItemCandidateDto.builder()
                .id(item.getId())
                .name(item.getName())
                .categoryName(item.getCategoryName())
                .categoryId(item.getCategoryId())
                .size(calculatedSize)
                .weightKg(item.getWeightKg())
                .dimensions(item.getDimensions())
                .quantity(item.getQuantity())
                .isFragile(item.getIsFragile())
                .requiresDisassembly(item.getRequiresDisassembly())
                .requiresPackaging(item.getRequiresPackaging())
                .source(item.getSource())
                .confidence(item.getConfidence())
                .imageUrl(item.getImageUrl())
                .notes(item.getNotes())
                .metadata(item.getMetadata())
                .build();
        }

        return item;
    }

    /**
     * Infer size from category and name when weight/dimensions unavailable
     */
    private String inferSizeFromCategory(String category, String name) {
        if (category == null && name == null) {
            return "M";
        }

        String lowerName = name != null ? name.toLowerCase() : "";
        String lowerCategory = category != null ? category.toLowerCase() : "";

        // Large items
        if (lowerName.contains("tủ lạnh") || lowerName.contains("refrigerator") ||
            lowerName.contains("máy giặt") || lowerName.contains("washing machine") ||
            lowerName.contains("giường") || lowerName.contains("bed") ||
            lowerName.contains("tủ quần áo") || lowerName.contains("wardrobe") ||
            lowerName.contains("bộ bàn ghế") || lowerName.contains("sofa") ||
            lowerName.contains("bàn ăn") || lowerName.contains("dining table")) {
            return "L";
        }

        // Small items
        if (lowerName.contains("ghế") || lowerName.contains("chair") ||
            lowerName.contains("bàn trà") || lowerName.contains("coffee table") ||
            lowerCategory.contains("box") ||
            lowerName.contains("thùng") || lowerName.contains("carton")) {
            return "S";
        }

        // Medium items (default)
        return "M";
    }
}

