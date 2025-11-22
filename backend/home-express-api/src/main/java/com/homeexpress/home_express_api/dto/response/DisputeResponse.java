package com.homeexpress.home_express_api.dto.response;

import com.homeexpress.home_express_api.entity.DisputeStatus;
import com.homeexpress.home_express_api.entity.DisputeType;
import com.homeexpress.home_express_api.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for dispute information.
 * Includes enriched data about the dispute, filer, and resolver.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeResponse {

    private Long disputeId;
    private Long bookingId;
    private String bookingStatus;
    
    // Filed by information
    private Long filedByUserId;
    private String filedByUserName;
    private UserRole filedByUserRole;
    
    // Dispute details
    private DisputeType disputeType;
    private DisputeStatus status;
    private String title;
    private String description;
    private String requestedResolution;
    
    // Resolution information
    private String resolutionNotes;
    private Long resolvedByUserId;
    private String resolvedByUserName;
    private LocalDateTime resolvedAt;
    
    // Counts
    private Long messageCount;
    private Long evidenceCount;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

