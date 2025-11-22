package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CategoryPricingRequest {

    @NotNull(message = "Transport ID is required")
    private Long transportId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Long sizeId;

    @NotNull(message = "Price per unit is required")
    @DecimalMin(value = "0.0", message = "Price per unit must be non-negative")
    private BigDecimal pricePerUnitVnd;

    @NotNull(message = "Fragile multiplier is required")
    @DecimalMin(value = "1.00", message = "Fragile multiplier must be at least 1.00")
    @DecimalMax(value = "3.00", message = "Fragile multiplier must not exceed 3.00")
    private BigDecimal fragileMultiplier = BigDecimal.valueOf(1.20);

    @NotNull(message = "Disassembly multiplier is required")
    @DecimalMin(value = "1.00", message = "Disassembly multiplier must be at least 1.00")
    @DecimalMax(value = "3.00", message = "Disassembly multiplier must not exceed 3.00")
    private BigDecimal disassemblyMultiplier = BigDecimal.valueOf(1.30);

    @NotNull(message = "Heavy multiplier is required")
    @DecimalMin(value = "1.00", message = "Heavy multiplier must be at least 1.00")
    @DecimalMax(value = "5.00", message = "Heavy multiplier must not exceed 5.00")
    private BigDecimal heavyMultiplier = BigDecimal.valueOf(1.50);

    @NotNull(message = "Heavy threshold is required")
    @DecimalMin(value = "0.01", message = "Heavy threshold must be positive")
    private BigDecimal heavyThresholdKg = BigDecimal.valueOf(100.00);

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    private LocalDateTime validTo;

    public CategoryPricingRequest() {}

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSizeId() {
        return sizeId;
    }

    public void setSizeId(Long sizeId) {
        this.sizeId = sizeId;
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
}
