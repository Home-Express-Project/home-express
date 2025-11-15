package com.homeexpress.home_express_api.dto.response;

import com.homeexpress.home_express_api.entity.Vehicle;
import com.homeexpress.home_express_api.entity.VehicleStatus;
import com.homeexpress.home_express_api.entity.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VehicleResponse {

    private Long vehicleId;
    private Long transportId;
    private VehicleType type;
    private String model;
    private String licensePlate;
    private String licensePlateNorm;
    private String licensePlateCompact;
    private BigDecimal capacityKg;
    private BigDecimal capacityM3;
    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private VehicleStatus status;
    private Short year;
    private String color;
    private Boolean hasTailLift;
    private Boolean hasTools;
    private String imageUrl;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VehicleResponse() {}

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setVehicleId(vehicle.getVehicleId());
        response.setTransportId(vehicle.getTransport().getTransportId());
        response.setType(vehicle.getType());
        response.setModel(vehicle.getModel());
        response.setLicensePlate(vehicle.getLicensePlate());
        response.setLicensePlateNorm(vehicle.getLicensePlateNorm());
        response.setLicensePlateCompact(vehicle.getLicensePlateCompact());
        response.setCapacityKg(vehicle.getCapacityKg());
        response.setCapacityM3(vehicle.getCapacityM3());
        response.setLengthCm(vehicle.getLengthCm());
        response.setWidthCm(vehicle.getWidthCm());
        response.setHeightCm(vehicle.getHeightCm());
        response.setStatus(vehicle.getStatus());
        response.setYear(vehicle.getYear());
        response.setColor(vehicle.getColor());
        response.setHasTailLift(vehicle.getHasTailLift());
        response.setHasTools(vehicle.getHasTools());
        response.setImageUrl(vehicle.getImageUrl());
        response.setDescription(vehicle.getDescription());
        response.setCreatedAt(vehicle.getCreatedAt());
        response.setUpdatedAt(vehicle.getUpdatedAt());
        return response;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

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

    public String getLicensePlateNorm() {
        return licensePlateNorm;
    }

    public void setLicensePlateNorm(String licensePlateNorm) {
        this.licensePlateNorm = licensePlateNorm;
    }

    public String getLicensePlateCompact() {
        return licensePlateCompact;
    }

    public void setLicensePlateCompact(String licensePlateCompact) {
        this.licensePlateCompact = licensePlateCompact;
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

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
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
