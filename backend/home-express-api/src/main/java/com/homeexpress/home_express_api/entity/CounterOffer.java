package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a counter-offer in price negotiation
 */
@Entity
@Table(name = "counter_offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CounterOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "counter_offer_id")
    private Long counterOfferId;

    @Column(name = "quotation_id", nullable = false)
    private Long quotationId;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    // Offer details
    @Column(name = "offered_by_user_id", nullable = false)
    private Long offeredByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "offered_by_role", nullable = false)
    private CounterOfferRole offeredByRole;

    // Pricing
    @Column(name = "offered_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal offeredPrice;

    @Column(name = "original_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal originalPrice;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CounterOfferStatus status = CounterOfferStatus.PENDING;

    // Negotiation details
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "reason", length = 500)
    private String reason;

    // Response tracking
    @Column(name = "responded_by_user_id")
    private Long respondedByUserId;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "response_message", columnDefinition = "TEXT")
    private String responseMessage;

    // Expiration
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Set default expiration to 24 hours from now if not set
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusHours(24);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if this counter-offer has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if this counter-offer is still pending
     */
    public boolean isPending() {
        return status == CounterOfferStatus.PENDING && !isExpired();
    }

    /**
     * Check if this counter-offer can be responded to
     */
    public boolean canBeRespondedTo() {
        return status == CounterOfferStatus.PENDING && !isExpired();
    }
}

