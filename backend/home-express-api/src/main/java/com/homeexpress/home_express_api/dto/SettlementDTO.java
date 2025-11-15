package com.homeexpress.home_express_api.dto;

import com.homeexpress.home_express_api.entity.CollectionMode;
import com.homeexpress.home_express_api.entity.SettlementStatus;

import java.time.LocalDateTime;

public class SettlementDTO {

    private Long settlementId;
    private Long bookingId;
    private Long transportId;
    private Long agreedPriceVnd;
    private Long totalCollectedVnd;
    private Long gatewayFeeVnd;
    private Integer commissionRateBps;
    private Long platformFeeVnd;
    private Long adjustmentVnd;
    private Long netToTransportVnd;
    private CollectionMode collectionMode;
    private SettlementStatus status;
    private String onHoldReason;
    private LocalDateTime createdAt;
    private LocalDateTime readyAt;
    private LocalDateTime paidAt;
    private LocalDateTime updatedAt;
    private String notes;

    public SettlementDTO() {
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

    public void setNetToTransportVnd(Long netToTransportVnd) {
        this.netToTransportVnd = netToTransportVnd;
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
}
