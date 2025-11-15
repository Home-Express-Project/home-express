package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_pricing")
public class VehiclePricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_pricing_id")
    private Long vehiclePricingId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_id", nullable = false)
    private Transport transport;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @NotNull
    @Column(name = "base_price_vnd", nullable = false, precision = 12)
    private BigDecimal basePriceVnd;

    @NotNull
    @Column(name = "per_km_first_4km_vnd", nullable = false, precision = 12)
    private BigDecimal perKmFirst4KmVnd;

    @NotNull
    @Column(name = "per_km_5_to_40km_vnd", nullable = false, precision = 12)
    private BigDecimal perKm5To40KmVnd;

    @NotNull
    @Column(name = "per_km_after_40km_vnd", nullable = false, precision = 12)
    private BigDecimal perKmAfter40KmVnd;

    @Column(name = "min_charge_vnd", precision = 12)
    private BigDecimal minChargeVnd;

    @NotNull
    @Column(name = "elevator_bonus_vnd", nullable = false, precision = 12)
    private BigDecimal elevatorBonusVnd = BigDecimal.ZERO;

    @NotNull
    @Column(name = "no_elevator_fee_per_floor_vnd", nullable = false, precision = 12)
    private BigDecimal noElevatorFeePerFloorVnd = BigDecimal.ZERO;

    @NotNull
    @Column(name = "no_elevator_floor_threshold", nullable = false)
    private Integer noElevatorFloorThreshold = 3;

    @NotNull
    @Column(name = "peak_hour_multiplier", nullable = false)
    private BigDecimal peakHourMultiplier = BigDecimal.valueOf(1.00);

    @NotNull
    @Column(name = "weekend_multiplier", nullable = false)
    private BigDecimal weekendMultiplier = BigDecimal.valueOf(1.00);

    @Column(name = "peak_hour_start_1")
    private Integer peakHourStart1 = 7;

    @Column(name = "peak_hour_end_1")
    private Integer peakHourEnd1 = 9;

    @Column(name = "peak_hour_start_2")
    private Integer peakHourStart2 = 17;

    @Column(name = "peak_hour_end_2")
    private Integer peakHourEnd2 = 19;

    @NotNull
    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "Asia/Ho_Chi_Minh";

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

    public VehiclePricing() {}

    public Long getVehiclePricingId() {
        return vehiclePricingId;
    }

    public void setVehiclePricingId(Long vehiclePricingId) {
        this.vehiclePricingId = vehiclePricingId;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
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
