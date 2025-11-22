package com.homeexpress.home_express_api.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SubmitQuotationRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;

    @NotNull(message = "Distance price is required")
    private BigDecimal distancePrice;

    private BigDecimal itemHandlingPrice;

    private BigDecimal additionalServicesPrice;

    private Boolean includesPackaging = false;

    private Boolean includesDisassembly = false;

    private Boolean includesInsurance = false;

    private BigDecimal insuranceValue;

    @NotNull(message = "Estimated duration is required")
    @Positive(message = "Estimated duration must be positive")
    private BigDecimal estimatedDurationHours;

    private String estimatedStartTime;

    private String notes;

    // Constructors
    public SubmitQuotationRequest() {}

    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getDistancePrice() {
        return distancePrice;
    }

    public void setDistancePrice(BigDecimal distancePrice) {
        this.distancePrice = distancePrice;
    }

    public BigDecimal getItemHandlingPrice() {
        return itemHandlingPrice;
    }

    public void setItemHandlingPrice(BigDecimal itemHandlingPrice) {
        this.itemHandlingPrice = itemHandlingPrice;
    }

    public BigDecimal getAdditionalServicesPrice() {
        return additionalServicesPrice;
    }

    public void setAdditionalServicesPrice(BigDecimal additionalServicesPrice) {
        this.additionalServicesPrice = additionalServicesPrice;
    }

    public Boolean getIncludesPackaging() {
        return includesPackaging;
    }

    public void setIncludesPackaging(Boolean includesPackaging) {
        this.includesPackaging = includesPackaging;
    }

    public Boolean getIncludesDisassembly() {
        return includesDisassembly;
    }

    public void setIncludesDisassembly(Boolean includesDisassembly) {
        this.includesDisassembly = includesDisassembly;
    }

    public Boolean getIncludesInsurance() {
        return includesInsurance;
    }

    public void setIncludesInsurance(Boolean includesInsurance) {
        this.includesInsurance = includesInsurance;
    }

    public BigDecimal getInsuranceValue() {
        return insuranceValue;
    }

    public void setInsuranceValue(BigDecimal insuranceValue) {
        this.insuranceValue = insuranceValue;
    }

    public BigDecimal getEstimatedDurationHours() {
        return estimatedDurationHours;
    }

    public void setEstimatedDurationHours(BigDecimal estimatedDurationHours) {
        this.estimatedDurationHours = estimatedDurationHours;
    }

    public String getEstimatedStartTime() {
        return estimatedStartTime;
    }

    public void setEstimatedStartTime(String estimatedStartTime) {
        this.estimatedStartTime = estimatedStartTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
