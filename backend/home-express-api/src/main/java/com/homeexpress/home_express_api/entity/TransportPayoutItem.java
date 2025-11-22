package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * TransportPayoutItem entity represents individual line items in a payout batch.
 * Each item links to a booking settlement that is included in the payout.
 */
@Entity
@Table(name = "transport_payout_items")
public class TransportPayoutItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payout_item_id")
    private Long payoutItemId;

    @NotNull
    @Column(name = "payout_id", nullable = false)
    private Long payoutId;

    @NotNull
    @Column(name = "settlement_id", nullable = false)
    private Long settlementId;

    @NotNull
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @NotNull
    @Positive
    @Column(name = "amount_vnd", nullable = false)
    private Long amountVnd;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public TransportPayoutItem() {
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
