package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class SizeRequest {
    
    @NotBlank(message = "Size name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private BigDecimal widthCm;
    private BigDecimal depthCm;
    
    @NotNull(message = "Price multiplier is required")
    @DecimalMin(value = "0.10", message = "Price multiplier must be at least 0.10")
    @DecimalMax(value = "10.00", message = "Price multiplier must not exceed 10.00")
    private BigDecimal priceMultiplier;

    public SizeRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public BigDecimal getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(BigDecimal widthCm) {
        this.widthCm = widthCm;
    }

    public BigDecimal getDepthCm() {
        return depthCm;
    }

    public void setDepthCm(BigDecimal depthCm) {
        this.depthCm = depthCm;
    }

    public BigDecimal getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setPriceMultiplier(BigDecimal priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }
}
