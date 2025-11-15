package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class RateCardRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", message = "Base price must be non-negative")
    private BigDecimal basePrice;

    @NotNull(message = "Price per km is required")
    @DecimalMin(value = "0.0", message = "Price per km must be non-negative")
    private BigDecimal pricePerKm;

    @NotNull(message = "Price per hour is required")
    @DecimalMin(value = "0.0", message = "Price per hour must be non-negative")
    private BigDecimal pricePerHour;

    @NotNull(message = "Minimum charge is required")
    @DecimalMin(value = "0.0", message = "Minimum charge must be non-negative")
    private BigDecimal minimumCharge;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid until date is required")
    private LocalDateTime validUntil;

    /**
     * Flexible pricing rules for future dynamic pricing implementation.
     * Example keys: "fragile_multiplier", "disassembly_multiplier", "heavy_item_multiplier".
     */
    private Map<String, BigDecimal> additionalRules;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getPricePerKm() {
        return pricePerKm;
    }

    public void setPricePerKm(BigDecimal pricePerKm) {
        this.pricePerKm = pricePerKm;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public BigDecimal getMinimumCharge() {
        return minimumCharge;
    }

    public void setMinimumCharge(BigDecimal minimumCharge) {
        this.minimumCharge = minimumCharge;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public Map<String, BigDecimal> getAdditionalRules() {
        return additionalRules;
    }

    public void setAdditionalRules(Map<String, BigDecimal> additionalRules) {
        this.additionalRules = additionalRules;
    }
}

