package com.homeexpress.home_express_api.dto.transport;

import com.homeexpress.home_express_api.entity.CollectionMode;
import com.homeexpress.home_express_api.entity.SettlementStatus;

import java.time.LocalDateTime;

public class TransportSettlementListDTO {

    private Long settlementId;
    private Long bookingId;
    private Long agreedPriceVnd;
    private Long netToTransportVnd;
    private CollectionMode collectionMode;
    private SettlementStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime readyAt;
    private LocalDateTime paidAt;

    public TransportSettlementListDTO() {
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

    public Long getAgreedPriceVnd() {
        return agreedPriceVnd;
    }

    public void setAgreedPriceVnd(Long agreedPriceVnd) {
        this.agreedPriceVnd = agreedPriceVnd;
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
}
