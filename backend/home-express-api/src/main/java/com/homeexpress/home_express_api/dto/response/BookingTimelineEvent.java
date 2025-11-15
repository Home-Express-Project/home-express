package com.homeexpress.home_express_api.dto.response;

import com.homeexpress.home_express_api.entity.ActorRole;
import com.homeexpress.home_express_api.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Timeline event for booking history view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingTimelineEvent {
    private LocalDateTime timestamp;
    private String eventType; // "STATUS_CHANGE", "QUOTATION_SUBMITTED", "QUOTATION_ACCEPTED", "PAYMENT_RECEIVED"
    private BookingStatus status; // For status changes
    private ActorRole actorRole;
    private Long actorId;
    private String actorName; // User name or system
    private String description;
    private Long referenceId; // Quotation ID, Payment ID, etc.
    private String referenceType; // "QUOTATION", "PAYMENT", etc.
}

