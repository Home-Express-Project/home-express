package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_settlements")
public class BookingSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long settlementId;

    @NotNull
    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @NotNull
    @Column(name = "transport_id", nullable = false)
    private Long transportId;

    @NotNull
    @Column(name = "agreed_price_vnd", nullable = false)
    private Long agreedPriceVnd;

    @NotNull
    @Column(name = "deposit_paid_vnd", nullable = false)
    private Long depositPaidVnd = 0L;

    @NotNull
    @Column(name = "remaining_paid_vnd", nullable = false)
    private Long remainingPaidVnd = 0L;

    @NotNull
    @Column(name = "tip_vnd", nullable = false)
    private Long tipVnd = 0L;

    @NotNull
    @Column(name = "total_collected_vnd", nullable = false)
    private Long totalCollectedVnd = 0L;

    /**
     * Bank transfer / payment gateway fee hook.
     * In this version we do not apply any gateway markup, so this is always 0 VND.
     */
    @NotNull
    @Column(name = "gateway_fee_vnd", nullable = false)
    private Long gatewayFeeVnd = 0L;

    @NotNull
    @Column(name = "commission_rate_bps", nullable = false)
    private Integer commissionRateBps = 0;

    @NotNull
    @Column(name = "platform_fee_vnd", nullable = false)
    private Long platformFeeVnd = 0L;

    @NotNull
    @Column(name = "adjustment_vnd", nullable = false)
    private Long adjustmentVnd = 0L;

    @Column(name = "net_to_transport_vnd", insertable = false, updatable = false)
    private Long netToTransportVnd;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "collection_mode", nullable = false, length = 20)
    private CollectionMode collectionMode = CollectionMode.ALL_ONLINE;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "on_hold_reason", columnDefinition = "TEXT")
    private String onHoldReason;

    @Column(name = "payout_id")
    private Long payoutId;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "ready_at")
    private LocalDateTime readyAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    public BookingSettlement() {
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

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public Long getAgreedPriceVnd() {
        return agreedPriceVnd;
    }

    public void setAgreedPriceVnd(Long agreedPriceVnd) {
        this.agreedPriceVnd = agreedPriceVnd;
    }

    public Long getDepositPaidVnd() {
        return depositPaidVnd;
    }

    public void setDepositPaidVnd(Long depositPaidVnd) {
        this.depositPaidVnd = depositPaidVnd;
    }

    public Long getRemainingPaidVnd() {
        return remainingPaidVnd;
    }

    public void setRemainingPaidVnd(Long remainingPaidVnd) {
        this.remainingPaidVnd = remainingPaidVnd;
    }

    public Long getTipVnd() {
        return tipVnd;
    }

    public void setTipVnd(Long tipVnd) {
        this.tipVnd = tipVnd;
    }

    public Long getTotalCollectedVnd() {
        return totalCollectedVnd;
    }

    public void setTotalCollectedVnd(Long totalCollectedVnd) {
        this.totalCollectedVnd = totalCollectedVnd;
    }

    public Long getGatewayFeeVnd() {
        return gatewayFeeVnd;
    }

    public void setGatewayFeeVnd(Long gatewayFeeVnd) {
        this.gatewayFeeVnd = gatewayFeeVnd;
    }

    public Integer getCommissionRateBps() {
        return commissionRateBps;
    }

    public void setCommissionRateBps(Integer commissionRateBps) {
        this.commissionRateBps = commissionRateBps;
    }

    public Long getPlatformFeeVnd() {
        return platformFeeVnd;
    }

    public void setPlatformFeeVnd(Long platformFeeVnd) {
        this.platformFeeVnd = platformFeeVnd;
    }

    public Long getAdjustmentVnd() {
        return adjustmentVnd;
    }

    public void setAdjustmentVnd(Long adjustmentVnd) {
        this.adjustmentVnd = adjustmentVnd;
    }

    public Long getNetToTransportVnd() {
        return netToTransportVnd;
    }

    public Long getTransportAmountVnd() {
        return netToTransportVnd;
    }

    public Long getPayoutId() {
        return payoutId;
    }

    public void setPayoutId(Long payoutId) {
        this.payoutId = payoutId;
    }

    public CollectionMode getCollectionMode() {
        return collectionMode;
    }

    public void setCollectionMode(CollectionMode collectionMode) {
        this.collectionMode = collectionMode;
    }

    public SettlementStatus getStatus() {
        return status;
    }

    public void setStatus(SettlementStatus status) {
        this.status = status;
    }

    public String getOnHoldReason() {
        return onHoldReason;
    }

    public void setOnHoldReason(String onHoldReason) {
        this.onHoldReason = onHoldReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(LocalDateTime readyAt) {
        this.readyAt = readyAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
