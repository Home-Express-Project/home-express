package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VehiclePricingRequest {

    @NotNull(message = "Transport ID is required")
    private Long transportId;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", message = "Base price must be non-negative")
    private BigDecimal basePriceVnd;

    @NotNull(message = "Price per km (first 4km) is required")
    @DecimalMin(value = "0.0", message = "Price per km must be non-negative")
    private BigDecimal perKmFirst4KmVnd;

    @NotNull(message = "Price per km (5-40km) is required")
    @DecimalMin(value = "0.0", message = "Price per km must be non-negative")
    private BigDecimal perKm5To40KmVnd;

    @NotNull(message = "Price per km (after 40km) is required")
    @DecimalMin(value = "0.0", message = "Price per km must be non-negative")
    private BigDecimal perKmAfter40KmVnd;

    @DecimalMin(value = "0.0", message = "Minimum charge must be non-negative")
    private BigDecimal minChargeVnd;

    @DecimalMin(value = "0.0", message = "Elevator bonus must be non-negative")
    private BigDecimal elevatorBonusVnd = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "No elevator fee must be non-negative")
    private BigDecimal noElevatorFeePerFloorVnd = BigDecimal.ZERO;

    @Min(value = 0, message = "Floor threshold must be non-negative")
    private Integer noElevatorFloorThreshold = 3;

    @DecimalMin(value = "1.00", message = "Peak hour multiplier must be at least 1.00")
    private BigDecimal peakHourMultiplier = BigDecimal.valueOf(1.00);

    @DecimalMin(value = "1.00", message = "Weekend multiplier must be at least 1.00")
    private BigDecimal weekendMultiplier = BigDecimal.valueOf(1.00);

    @Min(value = 0, message = "Hour must be between 0 and 23")
    @Max(value = 23, message = "Hour must be between 0 and 23")
    private Integer peakHourStart1 = 7;

    @Min(value = 0, message = "Hour must be between 0 and 23")
    @Max(value = 23, message = "Hour must be between 0 and 23")
    private Integer peakHourEnd1 = 9;

    @Min(value = 0, message = "Hour must be between 0 and 23")
    @Max(value = 23, message = "Hour must be between 0 and 23")
    private Integer peakHourStart2 = 17;

    @Min(value = 0, message = "Hour must be between 0 and 23")
    @Max(value = 23, message = "Hour must be between 0 and 23")
    private Integer peakHourEnd2 = 19;

    private String timezone = "Asia/Ho_Chi_Minh";

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    private LocalDateTime validTo;

    public VehiclePricingRequest() {}

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
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
