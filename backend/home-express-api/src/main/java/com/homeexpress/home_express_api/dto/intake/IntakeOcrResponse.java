package com.homeexpress.home_express_api.dto.intake;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response payload for OCR processing requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntakeOcrResponse {

    private boolean success;
    private OcrData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OcrData {
        private String extractedText;
        private List<ItemCandidateDto> candidates;
    }
}
