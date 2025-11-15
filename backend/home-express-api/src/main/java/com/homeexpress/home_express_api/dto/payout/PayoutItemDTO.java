package com.homeexpress.home_express_api.dto.payout;

import com.homeexpress.home_express_api.entity.TransportPayoutItem;

import java.time.LocalDateTime;

/**
 * DTO for TransportPayoutItem entity.
 * Used to transfer payout item data between layers.
 */
public class PayoutItemDTO {

    private Long payoutItemId;
    private Long payoutId;
    private Long settlementId;
    private Long bookingId;
    private Long amountVnd;
    private LocalDateTime createdAt;

    public PayoutItemDTO() {
    }

    public static PayoutItemDTO fromEntity(TransportPayoutItem item) {
        PayoutItemDTO dto = new PayoutItemDTO();
        dto.setPayoutItemId(item.getPayoutItemId());
        dto.setPayoutId(item.getPayoutId());
        dto.setSettlementId(item.getSettlementId());
        dto.setBookingId(item.getBookingId());
        dto.setAmountVnd(item.getAmountVnd());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    public Long getPayoutItemId() {
        return payoutItemId;
    }

    public void setPayoutItemId(Long payoutItemId) {
        this.payoutItemId = payoutItemId;
    }

    public Long getPayoutId() {
        return payoutId;
    }

    public void setPayoutId(Long payoutId) {
        this.payoutId = payoutId;
    }

    public Long getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(Long settlementId) {
        this.settlementId = settlementId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getAmountVnd() {
        return amountVnd;
    }

    public void setAmountVnd(Long amountVnd) {
        this.amountVnd = amountVnd;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
