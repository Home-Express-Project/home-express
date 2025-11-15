package com.homeexpress.home_express_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class QuotationDetailResponse {

    private Long quotationId;
    private Long bookingId;
    private Long transportId;

    // Pricing breakdown
    private BigDecimal basePrice;
    private BigDecimal distancePrice;
    private BigDecimal itemHandlingPrice;
    private BigDecimal additionalServicesPrice;
    private BigDecimal totalPrice;

    // Services
    private Boolean includesPackaging;
    private Boolean includesDisassembly;
    private Boolean includesInsurance;
    private BigDecimal insuranceValue;

    // Transport info (denormalized)
    private String transporterName;
    private String transporterAvatar;
    private Double transporterRating;
    private Integer transporterCompletedJobs;

    // Timing
    private BigDecimal estimatedDurationHours;
    private String estimatedStartTime;

    // Status
    private String status;
    private Boolean isSelected;
    private LocalDateTime expiresAt;

    private String notes;
    private String metadata;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;

    // Constructors
    public QuotationDetailResponse() {}

    // Getters and Setters
    public Long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(Long quotationId) {
        this.quotationId = quotationId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
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

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
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

    public String getTransporterName() {
        return transporterName;
    }

    public void setTransporterName(String transporterName) {
        this.transporterName = transporterName;
    }

    public String getTransporterAvatar() {
        return transporterAvatar;
    }

    public void setTransporterAvatar(String transporterAvatar) {
        this.transporterAvatar = transporterAvatar;
    }

    public Double getTransporterRating() {
        return transporterRating;
    }

    public void setTransporterRating(Double transporterRating) {
        this.transporterRating = transporterRating;
    }

    public Integer getTransporterCompletedJobs() {
        return transporterCompletedJobs;
    }

    public void setTransporterCompletedJobs(Integer transporterCompletedJobs) {
        this.transporterCompletedJobs = transporterCompletedJobs;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
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

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }
}
