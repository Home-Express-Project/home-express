package com.homeexpress.home_express_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "rate_card_snapshots")
public class RateCardSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Long snapshotId;

    @NotNull
    @Column(name = "quotation_id", nullable = false)
    private Long quotationId;

    @NotNull
    @Column(name = "transport_id", nullable = false)
    private Long transportId;

    @Column(name = "rate_card_id")
    private Long rateCardId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "pricing_snapshot", columnDefinition = "JSON")
    private String pricingSnapshot;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public RateCardSnapshot() {
    }

    public Long getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Long snapshotId) {
        this.snapshotId = snapshotId;
    }

    public Long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(Long quotationId) {
        this.quotationId = quotationId;
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public Long getRateCardId() {
        return rateCardId;
    }

    public void setRateCardId(Long rateCardId) {
        this.rateCardId = rateCardId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getPricingSnapshot() {
        return pricingSnapshot;
    }

    public void setPricingSnapshot(String pricingSnapshot) {
        this.pricingSnapshot = pricingSnapshot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

