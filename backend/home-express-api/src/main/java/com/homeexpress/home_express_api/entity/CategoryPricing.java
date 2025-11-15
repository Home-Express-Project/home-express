package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "category_pricing")
public class CategoryPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_pricing_id")
    private Long categoryPricingId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_id", nullable = false)
    private Transport transport;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id")
    private Size size;

    @NotNull
    @Column(name = "price_per_unit_vnd", nullable = false, precision = 12)
    private BigDecimal pricePerUnitVnd;

    @NotNull
    @Column(name = "fragile_multiplier", nullable = false)
    private BigDecimal fragileMultiplier = BigDecimal.valueOf(1.20);

    @NotNull
    @Column(name = "disassembly_multiplier", nullable = false)
    private BigDecimal disassemblyMultiplier = BigDecimal.valueOf(1.30);

    @NotNull
    @Column(name = "heavy_multiplier", nullable = false)
    private BigDecimal heavyMultiplier = BigDecimal.valueOf(1.50);

    @NotNull
    @Column(name = "heavy_threshold_kg", nullable = false)
    private BigDecimal heavyThresholdKg = BigDecimal.valueOf(100.00);

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom = LocalDateTime.now();

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public CategoryPricing() {}

    public Long getCategoryPricingId() {
        return categoryPricingId;
    }

    public void setCategoryPricingId(Long categoryPricingId) {
        this.categoryPricingId = categoryPricingId;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public BigDecimal getPricePerUnitVnd() {
        return pricePerUnitVnd;
    }

    public void setPricePerUnitVnd(BigDecimal pricePerUnitVnd) {
        this.pricePerUnitVnd = pricePerUnitVnd;
    }

    public BigDecimal getFragileMultiplier() {
        return fragileMultiplier;
    }

    public void setFragileMultiplier(BigDecimal fragileMultiplier) {
        this.fragileMultiplier = fragileMultiplier;
    }

    public BigDecimal getDisassemblyMultiplier() {
        return disassemblyMultiplier;
    }

    public void setDisassemblyMultiplier(BigDecimal disassemblyMultiplier) {
        this.disassemblyMultiplier = disassemblyMultiplier;
    }

    public BigDecimal getHeavyMultiplier() {
        return heavyMultiplier;
    }

    public void setHeavyMultiplier(BigDecimal heavyMultiplier) {
        this.heavyMultiplier = heavyMultiplier;
    }

    public BigDecimal getHeavyThresholdKg() {
        return heavyThresholdKg;
    }

    public void setHeavyThresholdKg(BigDecimal heavyThresholdKg) {
        this.heavyThresholdKg = heavyThresholdKg;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
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
}
