package com.homeexpress.home_express_api.dto.response;

import com.homeexpress.home_express_api.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for dispute message information.
 * Includes enriched data about the sender.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeMessageResponse {

    private Long messageId;
    private Long disputeId;
    
    // Sender information
    private Long senderUserId;
    private String senderName;
    private UserRole senderRole;
    
    // Message content
    private String messageText;
    
    // Timestamp
    private LocalDateTime createdAt;
}

