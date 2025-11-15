package com.homeexpress.home_express_api.dto.intake;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an item candidate from intake process
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ItemCandidateDto {
    
    private String id; // temporary UI ID
    private String name;
    private Integer categoryId;
    private String categoryName;
    
    // Size
    private String size; // S, M, L
    private Double weightKg;
    private DimensionsDto dimensions;
    
    // Quantity
    private Integer quantity;
    
    // Attributes
    private Boolean isFragile;
    private Boolean requiresDisassembly;
    private Boolean requiresPackaging;
    
    // Source & confidence
    private String source; // image, model, text, ocr, document, manual
    private Double confidence; // 0-1 for AI sources
    
    // Additional data
    private String imageUrl;
    private String notes;
    private Object metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DimensionsDto {
        private Double widthCm;
        private Double heightCm;
        private Double depthCm;
    }
}
