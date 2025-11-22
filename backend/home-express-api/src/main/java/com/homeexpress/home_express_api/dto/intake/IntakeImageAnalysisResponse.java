package com.homeexpress.home_express_api.dto.intake;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response payload for intake image analysis requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntakeImageAnalysisResponse {

    private boolean success;
    private AnalysisData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisData {
        private List<ItemCandidateDto> candidates;
        private AnalysisMetadata metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisMetadata {
        private String serviceUsed;
        private Double confidence;
        private Boolean fallbackUsed;
        private Boolean manualReviewRequired;
        private Boolean manualInputRequired;
        private Integer imageCount;
        private Long processingTimeMs;
        private Boolean fromCache;
        private Integer detectedItemCount;
        private Integer enhancedItemCount;
    }
}

