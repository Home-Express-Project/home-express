package com.homeexpress.home_express_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.homeexpress.home_express_api.entity.VehiclePricing;

public class VehiclePricingResponse {

    private Long vehiclePricingId;
    private Long transportId;
    private String transportCompanyName;
    private String vehicleType;
    private BigDecimal basePriceVnd;
    private BigDecimal perKmFirst4KmVnd;
    private BigDecimal perKm5To40KmVnd;
    private BigDecimal perKmAfter40KmVnd;
    private BigDecimal minChargeVnd;
    private BigDecimal elevatorBonusVnd;
    private BigDecimal noElevatorFeePerFloorVnd;
    private Integer noElevatorFloorThreshold;
    private BigDecimal peakHourMultiplier;
    private BigDecimal weekendMultiplier;
    private Integer peakHourStart1;
    private Integer peakHourEnd1;
    private Integer peakHourStart2;
    private Integer peakHourEnd2;
    private String timezone;
    private Boolean isActive;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VehiclePricingResponse() {
    }

    public static VehiclePricingResponse fromEntity(VehiclePricing entity) {
        VehiclePricingResponse response = new VehiclePricingResponse();
        response.setVehiclePricingId(entity.getVehiclePricingId());
        response.setTransportId(entity.getTransport().getTransportId());
        response.setTransportCompanyName(entity.getTransport().getCompanyName());
        response.setVehicleType(entity.getVehicleType().name());
        response.setBasePriceVnd(entity.getBasePriceVnd());
        response.setPerKmFirst4KmVnd(entity.getPerKmFirst4KmVnd());
        response.setPerKm5To40KmVnd(entity.getPerKm5To40KmVnd());
        response.setPerKmAfter40KmVnd(entity.getPerKmAfter40KmVnd());
        response.setMinChargeVnd(entity.getMinChargeVnd());
        response.setElevatorBonusVnd(entity.getElevatorBonusVnd());
        response.setNoElevatorFeePerFloorVnd(entity.getNoElevatorFeePerFloorVnd());
        response.setNoElevatorFloorThreshold(entity.getNoElevatorFloorThreshold());
        response.setPeakHourMultiplier(entity.getPeakHourMultiplier());
        response.setWeekendMultiplier(entity.getWeekendMultiplier());
        response.setPeakHourStart1(entity.getPeakHourStart1());
        response.setPeakHourEnd1(entity.getPeakHourEnd1());
        response.setPeakHourStart2(entity.getPeakHourStart2());
        response.setPeakHourEnd2(entity.getPeakHourEnd2());
        response.setTimezone(entity.getTimezone());
        response.setIsActive(entity.getIsActive());
        response.setValidFrom(entity.getValidFrom());
        response.setValidTo(entity.getValidTo());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public Long getVehiclePricingId() {
        return vehiclePricingId;
    }

    public void setVehiclePricingId(Long vehiclePricingId) {
        this.vehiclePricingId = vehiclePricingId;
    }

    /**
     * Alias for getVehiclePricingId() for backward compatibility Used by
     * controllers that return Map.of("pricingId", response.getPricingId())
     */
    public Long getPricingId() {
        return this.vehiclePricingId;
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public String getTransportCompanyName() {
        return transportCompanyName;
    }

    public void setTransportCompanyName(String transportCompanyName) {
        this.transportCompanyName = transportCompanyName;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public BigDecimal getBasePriceVnd() {
        return basePriceVnd;
    }

    public void setBasePriceVnd(BigDecimal basePriceVnd) {
        this.basePriceVnd = basePriceVnd;
    }

    public BigDecimal getPerKmFirst4KmVnd() {
        return perKmFirst4KmVnd;
    }

    public void setPerKmFirst4KmVnd(BigDecimal perKmFirst4KmVnd) {
        this.perKmFirst4KmVnd = perKmFirst4KmVnd;
    }

    public BigDecimal getPerKm5To40KmVnd() {
        return perKm5To40KmVnd;
    }

    public void setPerKm5To40KmVnd(BigDecimal perKm5To40KmVnd) {
        this.perKm5To40KmVnd = perKm5To40KmVnd;
    }

    public BigDecimal getPerKmAfter40KmVnd() {
        return perKmAfter40KmVnd;
    }

    public void setPerKmAfter40KmVnd(BigDecimal perKmAfter40KmVnd) {
        this.perKmAfter40KmVnd = perKmAfter40KmVnd;
    }

    public BigDecimal getMinChargeVnd() {
        return minChargeVnd;
    }

    public void setMinChargeVnd(BigDecimal minChargeVnd) {
        this.minChargeVnd = minChargeVnd;
    }

    public BigDecimal getElevatorBonusVnd() {
        return elevatorBonusVnd;
    }

    public void setElevatorBonusVnd(BigDecimal elevatorBonusVnd) {
        this.elevatorBonusVnd = elevatorBonusVnd;
    }

    public BigDecimal getNoElevatorFeePerFloorVnd() {
        return noElevatorFeePerFloorVnd;
    }

    public void setNoElevatorFeePerFloorVnd(BigDecimal noElevatorFeePerFloorVnd) {
        this.noElevatorFeePerFloorVnd = noElevatorFeePerFloorVnd;
    }

    public Integer getNoElevatorFloorThreshold() {
        return noElevatorFloorThreshold;
    }

    public void setNoElevatorFloorThreshold(Integer noElevatorFloorThreshold) {
        this.noElevatorFloorThreshold = noElevatorFloorThreshold;
    }

    public BigDecimal getPeakHourMultiplier() {
        return peakHourMultiplier;
    }

    public void setPeakHourMultiplier(BigDecimal peakHourMultiplier) {
        this.peakHourMultiplier = peakHourMultiplier;
    }

    public BigDecimal getWeekendMultiplier() {
        return weekendMultiplier;
    }

    public void setWeekendMultiplier(BigDecimal weekendMultiplier) {
        this.weekendMultiplier = weekendMultiplier;
    }

    public Integer getPeakHourStart1() {
        return peakHourStart1;
    }

    public void setPeakHourStart1(Integer peakHourStart1) {
        this.peakHourStart1 = peakHourStart1;
    }

    public Integer getPeakHourEnd1() {
        return peakHourEnd1;
    }

    public void setPeakHourEnd1(Integer peakHourEnd1) {
        this.peakHourEnd1 = peakHourEnd1;
    }

    public Integer getPeakHourStart2() {
        return peakHourStart2;
    }

    public void setPeakHourStart2(Integer peakHourStart2) {
        this.peakHourStart2 = peakHourStart2;
    }

    public Integer getPeakHourEnd2() {
        return peakHourEnd2;
    }

    public void setPeakHourEnd2(Integer peakHourEnd2) {
        this.peakHourEnd2 = peakHourEnd2;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
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
