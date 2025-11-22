package com.homeexpress.home_express_api.dto.intake;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response payload returned after parsing intake text.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntakeParseTextResponse {

    private boolean success;
    private ParseTextData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParseTextData {

        private List<ParsedItem> candidates;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<String> warnings;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Map<String, Object> metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParsedItem {

        private String name;
        private String brand;
        private String model;
        private Integer quantity;

        @JsonProperty("category_name")
        private String categoryName;

        private String size;
        
        @JsonProperty("is_fragile")
        private Boolean isFragile;
        
        @JsonProperty("requires_disassembly")
        private Boolean requiresDisassembly;
        
        private Double confidence;

        // Enhanced fields
        @JsonProperty("weight_kg")
        private Double weightKg;
        
        @JsonProperty("width_cm")
        private Double widthCm;
        
        @JsonProperty("height_cm")
        private Double heightCm;
        
        @JsonProperty("depth_cm")
        private Double depthCm;

        @JsonProperty("reasoning")
        private String reasoning;
    }
}

