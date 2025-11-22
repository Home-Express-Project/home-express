package com.homeexpress.home_express_api.dto.response;

import com.homeexpress.home_express_api.entity.CounterOffer;
import com.homeexpress.home_express_api.entity.CounterOfferRole;
import com.homeexpress.home_express_api.entity.CounterOfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for counter-offer data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounterOfferResponse {

    private Long counterOfferId;
    private Long quotationId;
    private Long bookingId;

    // Offer details
    private Long offeredByUserId;
    private String offeredByUserName;
    private CounterOfferRole offeredByRole;

    // Pricing
    private BigDecimal offeredPrice;
    private BigDecimal originalPrice;
    private BigDecimal priceDifference;
    private Double percentageChange;

    // Status
    private CounterOfferStatus status;
    private boolean isExpired;
    private boolean canRespond;

    // Negotiation details
    private String message;
    private String reason;

    // Response tracking
    private Long respondedByUserId;
    private String respondedByUserName;
    private LocalDateTime respondedAt;
    private String responseMessage;

    // Expiration
    private LocalDateTime expiresAt;
    private Long hoursUntilExpiration;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert entity to response DTO
     */
    public static CounterOfferResponse fromEntity(CounterOffer counterOffer) {
        return fromEntity(counterOffer, null, null);
    }

    /**
     * Convert entity to response DTO with user names
     */
    public static CounterOfferResponse fromEntity(
            CounterOffer counterOffer,
            String offeredByUserName,
            String respondedByUserName) {

        BigDecimal priceDiff = counterOffer.getOfferedPrice().subtract(counterOffer.getOriginalPrice());
        double percentChange = counterOffer.getOriginalPrice().doubleValue() != 0
                ? (priceDiff.doubleValue() / counterOffer.getOriginalPrice().doubleValue()) * 100
                : 0;

        Long hoursUntilExp = null;
        if (counterOffer.getExpiresAt() != null) {
            long seconds = java.time.Duration.between(LocalDateTime.now(), counterOffer.getExpiresAt()).getSeconds();
            hoursUntilExp = seconds / 3600;
        }

        return CounterOfferResponse.builder()
                .counterOfferId(counterOffer.getCounterOfferId())
                .quotationId(counterOffer.getQuotationId())
                .bookingId(counterOffer.getBookingId())
                .offeredByUserId(counterOffer.getOfferedByUserId())
                .offeredByUserName(offeredByUserName)
                .offeredByRole(counterOffer.getOfferedByRole())
                .offeredPrice(counterOffer.getOfferedPrice())
                .originalPrice(counterOffer.getOriginalPrice())
                .priceDifference(priceDiff)
                .percentageChange(percentChange)
                .status(counterOffer.getStatus())
                .isExpired(counterOffer.isExpired())
                .canRespond(counterOffer.canBeRespondedTo())
                .message(counterOffer.getMessage())
                .reason(counterOffer.getReason())
                .respondedByUserId(counterOffer.getRespondedByUserId())
                .respondedByUserName(respondedByUserName)
                .respondedAt(counterOffer.getRespondedAt())
                .responseMessage(counterOffer.getResponseMessage())
                .expiresAt(counterOffer.getExpiresAt())
                .hoursUntilExpiration(hoursUntilExp)
                .createdAt(counterOffer.getCreatedAt())
                .updatedAt(counterOffer.getUpdatedAt())
                .build();
    }
}

