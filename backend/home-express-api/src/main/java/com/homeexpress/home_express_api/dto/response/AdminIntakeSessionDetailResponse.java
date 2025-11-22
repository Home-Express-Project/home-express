package com.homeexpress.home_express_api.dto.response;

import com.homeexpress.home_express_api.dto.intake.ItemCandidateDto;
import com.homeexpress.home_express_api.service.intake.AdminIntakeSessionService.CustomerInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Detailed response for admin intake session view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminIntakeSessionDetailResponse {
    private String sessionId;
    private Long customerId;
    private String status;
    
    // Images
    private List<String> imageUrls;
    private Integer imageCount;
    
    // AI Detection results
    private String detectionResults; // JSON
    private Double averageConfidence;
    
    // Extracted items
    private List<ItemCandidateDto> items;
    
    // System estimation
    private Double estimatedPrice;
    private Double estimatedWeightKg;
    private Double estimatedVolumeM3;
    
    // Admin overrides
    private Double forcedQuotePrice;
    
    // Metadata
    private String aiServiceUsed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    
    // Customer info
    private CustomerInfo customer;
}

