package com.homeexpress.home_express_api.dto.request;

import com.homeexpress.home_express_api.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class VehicleRequest {

    @NotNull(message = "Vehicle type is required")
    private VehicleType type;

    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @NotBlank(message = "License plate is required")
    @Size(max = 20, message = "License plate must not exceed 20 characters")
    private String licensePlate;

    @NotNull(message = "Capacity (kg) is required")
    @Positive(message = "Capacity must be positive")
    private BigDecimal capacityKg;

    private BigDecimal capacityM3;
    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    
    private Short year;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    private Boolean hasTailLift = false;
    private Boolean hasTools = true;

    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;

    private String description;

    public VehicleRequest() {}

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public BigDecimal getCapacityKg() {
        return capacityKg;
    }

    public void setCapacityKg(BigDecimal capacityKg) {
        this.capacityKg = capacityKg;
    }

    public BigDecimal getCapacityM3() {
        return capacityM3;
    }

    public void setCapacityM3(BigDecimal capacityM3) {
        this.capacityM3 = capacityM3;
    }

    public BigDecimal getLengthCm() {
        return lengthCm;
    }

    public void setLengthCm(BigDecimal lengthCm) {
        this.lengthCm = lengthCm;
    }

    public BigDecimal getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(BigDecimal widthCm) {
        this.widthCm = widthCm;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public Short getYear() {
        return year;
    }

    public void setYear(Short year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getHasTailLift() {
        return hasTailLift;
    }

    public void setHasTailLift(Boolean hasTailLift) {
        this.hasTailLift = hasTailLift;
    }

    public Boolean getHasTools() {
        return hasTools;
    }

    public void setHasTools(Boolean hasTools) {
        this.hasTools = hasTools;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
