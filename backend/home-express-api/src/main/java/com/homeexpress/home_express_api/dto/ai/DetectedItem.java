package com.homeexpress.home_express_api.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single detected item from AI image analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectedItem {
    
    /**
     * Item category (furniture, appliance, electronics, box, etc.)
     */
    private String category;
    
    /**
     * Item name/description (e.g. "Refrigerator", "Sofa", "TV")
     */
    private String name;
    
    /**
     * AI confidence score (0.0 - 1.0)
     */
    private Double confidence;
    
    /**
     * Raw label from AI service (for debugging)
     */
    private String rawLabel;
    
    /**
     * Estimated dimensions (optional)
     */
    private ItemDimensions dimensions;
    
    /**
     * Image index (which photo this item was detected in)
     */
    private Integer imageIndex;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDimensions {
        private Double width;   // cm
        private Double height;  // cm
        private Double depth;   // cm
        private String unit;    // "cm", "m", "inch"
    }
}
