package com.homeexpress.home_express_api.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Enhanced Detected Item - Full logistics details
 * 
 * Used with ENHANCED_DETECTION_PROMPT for comprehensive item analysis
 * including dimensions, weight, material, handling requirements, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedDetectedItem {
    
    // Basic identification
    private String id;
    private String name;
    private String category;
    private String subcategory;
    private Integer quantity; // Number of identical items (default: 1)
    private Double confidence;
    
    // Bounding box (normalized coordinates 0-1)
    private BoundingBox bboxNorm;
    
    // Visual properties
    private Double occludedFraction;
    private String orientation; // upright, lying, unknown
    private List<String> material; // wood, fabric, metal, plastic, glass
    private String color;
    private String roomHint; // living_room, kitchen, bedroom, etc.
    
    // Dimensions (centimeters)
    private Dimensions dimsCm;
    private Double dimsConfidence;
    private String dimensionsBasis; // text-on-label, scale-from-reference, typical-category-range, visual-approximation, none
    private Double volumeM3;
    
    // Weight (kilograms)
    private String weightModel; // house-move-v1
    private Double weightKg;
    private Double weightConfidence;
    private String weightBasis; // label, known-model, category+size-prior, material-adjusted-prior, visual-approximation, none
    
    // Handling attributes
    private Boolean fragile;
    private Boolean twoPersonLift;
    private Boolean stackable;
    private Boolean disassemblyRequired;
    private String notes;
    
    // Brand & model (optional)
    private String brand;
    private String model;
    
    // Image reference
    private Integer imageIndex;
    
    /**
     * Bounding Box with normalized coordinates
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoundingBox {
        private Double xMin; // 0.0 - 1.0
        private Double yMin; // 0.0 - 1.0
        private Double xMax; // 0.0 - 1.0
        private Double yMax; // 0.0 - 1.0
    }
    
    /**
     * Item dimensions in centimeters
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dimensions {
        private Integer length; // cm (longest horizontal dimension)
        private Integer width;  // cm
        private Integer height; // cm (vertical dimension)
    }
    
    /**
     * Convert to basic DetectedItem for backward compatibility
     */
    public DetectedItem toBasicDetectedItem() {
        DetectedItem.ItemDimensions basicDims = null;
        if (dimsCm != null) {
            basicDims = DetectedItem.ItemDimensions.builder()
                .width(dimsCm.getWidth() != null ? dimsCm.getWidth().doubleValue() : null)
                .height(dimsCm.getHeight() != null ? dimsCm.getHeight().doubleValue() : null)
                .depth(dimsCm.getLength() != null ? dimsCm.getLength().doubleValue() : null)
                .unit("cm")
                .build();
        }
        
        return DetectedItem.builder()
            .name(name)
            .category(category)
            .confidence(confidence)
            .rawLabel(name != null ? name.toLowerCase() : null)
            .dimensions(basicDims)
            .imageIndex(imageIndex)
            .build();
    }

    /**
     * Build an enhanced item from a basic {@link DetectedItem} response.
     * Useful when upstream AI only returns the legacy payload.
     */
    public static EnhancedDetectedItem fromDetectedItem(DetectedItem item) {
        if (item == null) {
            return null;
        }

        Dimensions dims = null;
        if (item.getDimensions() != null) {
            dims = Dimensions.builder()
                .width(toInteger(item.getDimensions().getWidth()))
                .height(toInteger(item.getDimensions().getHeight()))
                .length(toInteger(item.getDimensions().getDepth()))
                .build();
        }

        return EnhancedDetectedItem.builder()
            .name(item.getName())
            .category(item.getCategory())
            .confidence(item.getConfidence())
            .quantity(1) // Default quantity
            .dimsCm(dims)
            .imageIndex(item.getImageIndex())
            .build();
    }

    private static Integer toInteger(Double value) {
        if (value == null) {
            return null;
        }
        return (int) Math.round(value);
    }
    
    /**
     * Validate item data
     */
    public boolean isValid() {
        // Basic validation
        if (name == null || name.isBlank()) return false;
        if (category == null || category.isBlank()) return false;
        if (confidence == null || confidence < 0.0 || confidence > 1.0) return false;
        
        // Bounding box validation (if present)
        if (bboxNorm != null) {
            if (bboxNorm.xMin == null || bboxNorm.yMin == null || 
                bboxNorm.xMax == null || bboxNorm.yMax == null) return false;
            if (bboxNorm.xMin < 0 || bboxNorm.xMin > 1) return false;
            if (bboxNorm.yMin < 0 || bboxNorm.yMin > 1) return false;
            if (bboxNorm.xMax < 0 || bboxNorm.xMax > 1) return false;
            if (bboxNorm.yMax < 0 || bboxNorm.yMax > 1) return false;
            if (bboxNorm.xMin >= bboxNorm.xMax) return false;
            if (bboxNorm.yMin >= bboxNorm.yMax) return false;
        }
        
        // Dimensions validation (if present)
        if (dimsCm != null) {
            if (dimsCm.length != null && dimsCm.length <= 0) return false;
            if (dimsCm.width != null && dimsCm.width <= 0) return false;
            if (dimsCm.height != null && dimsCm.height <= 0) return false;
        }
        
        // Weight validation (if present)
        if (weightKg != null && weightKg < 0) return false;
        
        return true;
    }
    
    /**
     * Calculate volume from dimensions
     */
    public Double calculateVolume() {
        if (dimsCm == null || dimsCm.length == null || 
            dimsCm.width == null || dimsCm.height == null) {
            return null;
        }
        
        // Convert cm³ to m³
        double volumeCm3 = dimsCm.length * dimsCm.width * dimsCm.height;
        return Math.round(volumeCm3 / 1_000_000.0 * 1000.0) / 1000.0; // Round to 3 decimals
    }
    
    /**
     * Get handling complexity score (0-10)
     * Higher score = more complex handling
     */
    public int getHandlingComplexity() {
        int score = 0;
        
        if (Boolean.TRUE.equals(fragile)) score += 3;
        if (Boolean.TRUE.equals(twoPersonLift)) score += 2;
        if (Boolean.FALSE.equals(stackable)) score += 1;
        if (Boolean.TRUE.equals(disassemblyRequired)) score += 3;
        
        if (weightKg != null) {
            if (weightKg > 50) score += 2;
            else if (weightKg > 100) score += 3;
        }
        
        if (volumeM3 != null) {
            if (volumeM3 > 1.0) score += 1;
            else if (volumeM3 > 2.0) score += 2;
        }
        
        return Math.min(score, 10);
    }
    
    /**
     * Get priority for loading order
     * Higher priority = load first (bottom of truck)
     */
    public int getLoadingPriority() {
        int priority = 5; // Default medium priority
        
        // Heavy items go first (bottom)
        if (weightKg != null && weightKg > 50) priority += 2;
        
        // Large items go first
        if (volumeM3 != null && volumeM3 > 1.0) priority += 1;
        
        // Fragile items go last (top)
        if (Boolean.TRUE.equals(fragile)) priority -= 3;
        
        // Stackable items can go earlier
        if (Boolean.TRUE.equals(stackable)) priority += 1;
        
        return Math.max(1, Math.min(priority, 10));
    }
    
    /**
     * Get estimated packing time in minutes
     */
    public int getEstimatedPackingTimeMinutes() {
        int baseTime = 5; // 5 minutes base
        
        if (Boolean.TRUE.equals(fragile)) baseTime += 10; // Extra wrapping
        if (Boolean.TRUE.equals(disassemblyRequired)) baseTime += 20; // Disassembly time
        if (Boolean.TRUE.equals(twoPersonLift)) baseTime += 5; // Coordination time
        
        if (volumeM3 != null && volumeM3 > 1.0) baseTime += 10; // Large items take longer
        
        return baseTime;
    }
    
    /**
     * Check if item requires special equipment
     */
    public boolean requiresSpecialEquipment() {
        // Heavy items need dolly/hand truck
        if (weightKg != null && weightKg > 50) return true;
        
        // Large items need straps/blankets
        if (volumeM3 != null && volumeM3 > 1.5) return true;
        
        // Fragile items need special packaging
        if (Boolean.TRUE.equals(fragile)) return true;
        
        // Disassembly requires tools
        if (Boolean.TRUE.equals(disassemblyRequired)) return true;
        
        return false;
    }
    
    /**
     * Get recommended box type for packing
     */
    public String getRecommendedBoxType() {
        if ("box".equals(category)) {
            return subcategory != null ? subcategory : "box_standard";
        }
        
        // For non-box items, recommend based on size
        if (volumeM3 == null) return "no_box_needed";
        
        if (volumeM3 < 0.05) return "small_box";
        if (volumeM3 < 0.1) return "medium_box";
        if (volumeM3 < 0.2) return "large_box";
        
        return "no_box_needed"; // Too large for standard boxes
    }
}
