package com.homeexpress.home_express_api.service.intake;

import com.homeexpress.home_express_api.dto.intake.ItemCandidateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service hậu xử lý (Post-processing) sau khi nhận diện đồ vật.
 * Nhiệm vụ:
 * - Gộp các món giống nhau (nếu quét từ nhiều ảnh).
 * - Chuẩn hóa tên tiếng Việt.
 * - Mapping danh mục về chuẩn của hệ thống.
 * - Cải thiện độ chính xác của số lượng (tách combo).
 */
@Slf4j
@Service
public class ItemDetectionPostProcessor {

    /**
     * Hàm xử lý chính: Gộp và làm sạch danh sách đồ vật.
     * Quy trình:
     * 1. Tách các bộ (Set) thành món lẻ.
     * 2. Chuẩn hóa tên, danh mục và tính kích thước.
     * 3. Gộp các món trùng nhau lại.
     */
    public List<ItemCandidateDto> processAndAggregate(List<ItemCandidateDto> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        log.info("Bắt đầu hậu xử lý cho {} món đồ tiềm năng", candidates.size());

        // Bước 1: Tách combo (VD: "Bộ bàn ghế" → "Bàn ăn" + "Ghế ăn")
        List<ItemCandidateDto> expanded = expandSets(candidates);

        // Bước 2: Chuẩn hóa tên, danh mục và tính lại kích thước (S/M/L)
        List<ItemCandidateDto> normalized = expanded.stream()
            .map(this::normalizeItem)
            .map(this::calculateSize)
            .collect(Collectors.toList());

        // Bước 3: Gộp các món tương tự nhau lại (tránh trùng lặp)
        List<ItemCandidateDto> aggregated = aggregateSimilarItems(normalized);

        log.info("Hậu xử lý hoàn tất: Từ {} món -> còn {} món sau khi gộp.", candidates.size(), aggregated.size());

        return aggregated;
    }

    /**
     * Tách các bộ (Set) thành từng món riêng lẻ để tính giá chính xác hơn.
     * Ví dụ:
     * - "Bộ bàn ghế" (1 bàn + 4 ghế) → "Bàn ăn" (1) + "Ghế ăn" (4)
     * - "Sofa set" → "Sofa" + "Bàn trà"
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
            
            // Kiểm tra xem có phải là bộ (Set) không
            if (isSetItem(name)) {
                List<ItemCandidateDto> setItems = breakDownSet(item);
                expanded.addAll(setItems);
            } else {
                // Không phải bộ thì giữ nguyên
                expanded.add(item);
            }
        }

        return expanded;
    }

    /**
     * Kiểm tra xem tên món đồ có phải là một bộ (Set) hay không.
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
     * Logic tách bộ thành các món lẻ.
     */
    private List<ItemCandidateDto> breakDownSet(ItemCandidateDto setItem) {
        List<ItemCandidateDto> items = new ArrayList<>();
        String name = setItem.getName().toLowerCase();

        // Sao chép metadata để các món con cũng có thông tin gốc
        Map<String, Object> baseMetadata = new HashMap<>();
        if (setItem.getMetadata() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> existingMetadata = (Map<String, Object>) setItem.getMetadata();
            baseMetadata.putAll(existingMetadata);
        }
        baseMetadata.put("fromSet", true);
        baseMetadata.put("originalSetName", setItem.getName());

        if (name.contains("bộ bàn ghế") || name.contains("dining set")) {
            // Bộ bàn ăn: 1 bàn + N ghế (mặc định 4)
            int chairCount = extractChairCount(setItem, 4);

            // Tạo bàn ăn
            items.add(createItemFromSet(
                setItem, "Bàn ăn", "furniture", "dining_table", 1, baseMetadata));

            // Tạo ghế ăn
            items.add(createItemFromSet(
                setItem, "Ghế ăn", "furniture", "dining_chair", chairCount, baseMetadata));

        } else if (name.contains("sofa set")) {
            // Bộ sofa: 1 sofa + 1 bàn trà
            items.add(createItemFromSet(
                setItem, "Sofa", "furniture", "sofa", 1, baseMetadata));
            items.add(createItemFromSet(
                setItem, "Bàn trà", "furniture", "coffee_table", 1, baseMetadata));
            
        } else if (name.contains("bedroom set")) {
            // Bộ phòng ngủ: 1 giường + 2 tủ đầu giường
            items.add(createItemFromSet(
                setItem, "Giường", "furniture", "bed_frame", 1, baseMetadata));
            items.add(createItemFromSet(
                setItem, "Tủ đầu giường", "furniture", "nightstand", 2, baseMetadata));
            
        } else {
            // Nếu không biết loại set gì thì log warning và giữ nguyên
            log.warn("Không rõ loại set: {}. Giữ nguyên như cũ.", setItem.getName());
            items.add(setItem);
        }

        return items;
    }

    /**
     * Cố gắng đoán số lượng ghế từ tên hoặc metadata (VD: "Bộ bàn ghế 6 chỗ" → 6).
     */
    private int extractChairCount(ItemCandidateDto item, int defaultValue) {
        // Kiểm tra metadata trước
        if (item.getMetadata() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) item.getMetadata();
            Object chairCountObj = metadata.get("chairCount");
            if (chairCountObj instanceof Number) {
                return ((Number) chairCountObj).intValue();
            }
        }

        // Thử tìm trong tên
        String name = item.getName();
        if (name != null) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*(chỗ|chair|ghế)");
            java.util.regex.Matcher matcher = pattern.matcher(name.toLowerCase());
            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    // Bỏ qua nếu lỗi parse số
                }
            }
        }

        return defaultValue;
    }

    /**
     * Hàm helper để tạo món con từ set mẹ.
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
     * Chuẩn hóa tên và danh mục món đồ (Hỗ trợ tiếng Việt).
     */
    private ItemCandidateDto normalizeItem(ItemCandidateDto item) {
        if (item == null || item.getName() == null) {
            return item;
        }

        String originalName = item.getName();
        String normalizedName = normalizeVietnameseName(originalName);
        String normalizedCategory = normalizeCategory(item.getCategoryName(), normalizedName);

        // Nếu tên hoặc danh mục thay đổi thì cập nhật lại object
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

            // Giữ lại metadata và đánh dấu là đã chuẩn hóa
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
     * Chuẩn hóa tên tiếng Việt (VD: "tủ lạnh" -> "Tủ lạnh").
     * Có map sẵn một số từ khóa phổ biến.
     */
    private String normalizeVietnameseName(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }

        String normalized = name.trim();

        // Map từ khóa tiếng Việt
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
                // Giữ lại tên hãng/model nếu có
                String brandModel = extractBrandModel(normalized);
                if (brandModel != null && !brandModel.isEmpty()) {
                    return entry.getValue() + " " + brandModel;
                }
                return entry.getValue();
            }
        }

        // Viết hoa chữ cái đầu nếu đang viết thường toàn bộ
        if (normalized.equals(normalized.toLowerCase()) && normalized.length() > 0) {
            return normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
        }

        return normalized;
    }

    /**
     * Bóc tách tên thương hiệu/model từ tên sản phẩm.
     */
    private String extractBrandModel(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        // Các thương hiệu phổ biến
        String[] brands = {"Samsung", "LG", "Sony", "Panasonic", "TCL", "Sharp", 
                          "Toshiba", "IKEA", "Xiaomi", "Electrolux", "Bosch", "Whirlpool"};
        
        for (String brand : brands) {
            if (name.toLowerCase().contains(brand.toLowerCase())) {
                return brand;
            }
        }

        // Tìm các mã model (có số trong tên)
        String[] parts = name.split("\\s+");
        for (String part : parts) {
            if (part.matches(".*\\d+.*") && part.length() >= 3) {
                return part;
            }
        }

        return null;
    }

    /**
     * Chuẩn hóa danh mục về bộ danh mục tiếng Việt của hệ thống.
     */
    private String normalizeCategory(String category, String itemName) {
        if (category == null) {
            category = inferCategoryFromName(itemName);
        }

        // Map danh mục tiếng Anh -> Tiếng Việt
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
     * Đoán danh mục từ tên sản phẩm (nếu AI không trả về danh mục).
     */
    private String inferCategoryFromName(String name) {
        if (name == null || name.isBlank()) {
            return "other";
        }

        String lowerName = name.toLowerCase();

        // Từ khóa Nội thất
        if (lowerName.matches(".*(sofa|bàn|ghế|giường|tủ|kệ|tủ quần áo|tủ sách|bộ bàn ghế).*")) {
            return "furniture";
        }

        // Từ khóa Điện lạnh
        if (lowerName.matches(".*(tủ lạnh|máy giặt|máy sấy|điều hòa|lò vi sóng|máy rửa chén).*")) {
            return "appliance";
        }

        // Từ khóa Điện tử
        if (lowerName.matches(".*(tivi|tv|máy tính|laptop|monitor|màn hình|printer|loa|speaker).*")) {
            return "electronics";
        }

        // Từ khóa Hộp/Thùng
        if (lowerName.matches(".*(thùng|box|carton|container|crate).*")) {
            return "box";
        }

        return "other";
    }

    /**
     * Gộp các món đồ giống hệt nhau (cùng tên, cùng danh mục).
     * Thường dùng khi quét nhiều ảnh và phát hiện trùng lặp.
     */
    private List<ItemCandidateDto> aggregateSimilarItems(List<ItemCandidateDto> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        // Nhóm theo Tên + Danh mục
        Map<String, List<ItemCandidateDto>> grouped = items.stream()
            .collect(Collectors.groupingBy(item -> {
                String name = item.getName() != null ? item.getName().toLowerCase().trim() : "";
                String category = item.getCategoryName() != null ? item.getCategoryName() : "";
                return name + "|" + category;
            }));

        List<ItemCandidateDto> aggregated = new ArrayList<>();

        for (List<ItemCandidateDto> group : grouped.values()) {
            if (group.size() == 1) {
                // Có 1 món thì không cần gộp
                aggregated.add(group.get(0));
            } else {
                // Có nhiều món trùng tên -> Gộp lại
                ItemCandidateDto aggregatedItem = aggregateItemGroup(group);
                aggregated.add(aggregatedItem);
            }
        }

        return aggregated;
    }

    /**
     * Logic gộp 1 nhóm item thành 1 item duy nhất.
     * - Cộng dồn số lượng.
     * - Lấy độ tin cậy trung bình.
     * - Lấy kích thước/cân nặng lớn nhất (an toàn).
     */
    private ItemCandidateDto aggregateItemGroup(List<ItemCandidateDto> group) {
        if (group == null || group.isEmpty()) {
            return null;
        }

        if (group.size() == 1) {
            return group.get(0);
        }

        // Lấy item đầu tiên làm gốc
        ItemCandidateDto base = group.get(0);

        // Cộng tổng số lượng
        int totalQuantity = group.stream()
            .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 1)
            .sum();

        // Tính độ tin cậy trung bình
        double avgConfidence = group.stream()
            .filter(item -> item.getConfidence() != null)
            .mapToDouble(ItemCandidateDto::getConfidence)
            .average()
            .orElse(base.getConfidence() != null ? base.getConfidence() : 0.8);

        // Lấy cân nặng lớn nhất (để tính giá cho chắc ăn)
        Double maxWeight = group.stream()
            .map(ItemCandidateDto::getWeightKg)
            .filter(Objects::nonNull)
            .max(Comparator.comparingDouble(weight -> weight != null ? weight : 0.0))
            .orElse(null);

        // Lấy kích thước lớn nhất
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

        // Gộp metadata lại
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
     * Tính size (S, M, L) dựa trên cân nặng và thể tích.
     * Quy tắc (từ frontend):
     * - S: < 20kg hoặc < 0.25 m³
     * - M: 20-50kg hoặc 0.25-0.85 m³
     * - L: > 50kg hoặc > 0.85 m³
     */
    private ItemCandidateDto calculateSize(ItemCandidateDto item) {
        if (item == null) {
            return item;
        }

        // Nếu đã có size rồi thì giữ nguyên
        if (item.getSize() != null && !item.getSize().isBlank()) {
            return item;
        }

        String calculatedSize = "M"; // Mặc định

        // Tính thể tích (m3)
        Double volumeM3 = null;
        if (item.getDimensions() != null) {
            Double width = item.getDimensions().getWidthCm();
            Double height = item.getDimensions().getHeightCm();
            Double depth = item.getDimensions().getDepthCm();
            
            if (width != null && height != null && depth != null) {
                volumeM3 = (width * height * depth) / 1_000_000.0; // Đổi cm3 sang m3
            }
        }

        // Ưu tiên tính theo cân nặng trước, sau đó đến thể tích
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
            if (volumeM3 < 0.25) {
                calculatedSize = "S";
            } else if (volumeM3 <= 0.85) {
                calculatedSize = "M";
            } else {
                calculatedSize = "L";
            }
        } else {
            // Nếu không có số liệu gì thì đoán theo tên/danh mục
            calculatedSize = inferSizeFromCategory(item.getCategoryName(), item.getName());
        }

        // Cập nhật size vào item
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
     * Đoán size dựa trên tên nếu không có thông số nào khác.
     */
    private String inferSizeFromCategory(String category, String name) {
        if (category == null && name == null) {
            return "M";
        }

        String lowerName = name != null ? name.toLowerCase() : "";
        String lowerCategory = category != null ? category.toLowerCase() : "";

        // Đồ lớn (L)
        if (lowerName.contains("tủ lạnh") || lowerName.contains("refrigerator") ||
            lowerName.contains("máy giặt") || lowerName.contains("washing machine") ||
            lowerName.contains("giường") || lowerName.contains("bed") ||
            lowerName.contains("tủ quần áo") || lowerName.contains("wardrobe") ||
            lowerName.contains("bộ bàn ghế") || lowerName.contains("sofa") ||
            lowerName.contains("bàn ăn") || lowerName.contains("dining table")) {
            return "L";
        }

        // Đồ nhỏ (S)
        if (lowerName.contains("ghế") || lowerName.contains("chair") ||
            lowerName.contains("bàn trà") || lowerName.contains("coffee table") ||
            lowerCategory.contains("box") ||
            lowerName.contains("thùng") || lowerName.contains("carton")) {
            return "S";
        }

        // Mặc định trung bình (M)
        return "M";
    }
}
