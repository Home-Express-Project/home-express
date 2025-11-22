package com.homeexpress.home_express_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.homeexpress.home_express_api.entity.QuotationStatus;

public class QuotationResponse {

    private Long quotationId;
    private Long bookingId;
    private Long transportId;
    private Long vehicleId;
    
    // Vehicle info for customer display
    private String vehicleModel;
    private String vehicleLicensePlate;
    private BigDecimal vehicleCapacityKg;
    private BigDecimal vehicleCapacityM3;
    
    private BigDecimal quotedPrice;
    private BigDecimal basePrice;
    private BigDecimal distancePrice;
    private BigDecimal itemsPrice;
    private BigDecimal additionalFees;
    private BigDecimal discount;
    private String priceBreakdown;
    private String notes;
    private Integer validityPeriod;
    private LocalDateTime expiresAt;
    private QuotationStatus status;
    private LocalDateTime respondedAt;
    private Long acceptedBy;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;

    // Constructors
    public QuotationResponse() {}

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

    public Long getVehicleId() { 
        return vehicleId; 
    }
    
    public void setVehicleId(Long vehicleId) { 
        this.vehicleId = vehicleId; 
    }

    public String getVehicleModel() { 
        return vehicleModel; 
    }
    
    public void setVehicleModel(String vehicleModel) { 
        this.vehicleModel = vehicleModel; 
    }

    public String getVehicleLicensePlate() { 
        return vehicleLicensePlate; 
    }
    
    public void setVehicleLicensePlate(String vehicleLicensePlate) { 
        this.vehicleLicensePlate = vehicleLicensePlate; 
    }

    public BigDecimal getVehicleCapacityKg() { 
        return vehicleCapacityKg; 
    }
    
    public void setVehicleCapacityKg(BigDecimal vehicleCapacityKg) { 
        this.vehicleCapacityKg = vehicleCapacityKg; 
    }

    public BigDecimal getVehicleCapacityM3() { 
        return vehicleCapacityM3; 
    }
    
    public void setVehicleCapacityM3(BigDecimal vehicleCapacityM3) { 
        this.vehicleCapacityM3 = vehicleCapacityM3; 
    }

    public BigDecimal getQuotedPrice() {
        return quotedPrice;
    }

    public void setQuotedPrice(BigDecimal quotedPrice) {
        this.quotedPrice = quotedPrice;
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

    public BigDecimal getItemsPrice() {
        return itemsPrice;
    }

    public void setItemsPrice(BigDecimal itemsPrice) {
        this.itemsPrice = itemsPrice;
    }

    public BigDecimal getAdditionalFees() {
        return additionalFees;
    }

    public void setAdditionalFees(BigDecimal additionalFees) {
        this.additionalFees = additionalFees;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public String getPriceBreakdown() {
        return priceBreakdown;
    }

    public void setPriceBreakdown(String priceBreakdown) {
        this.priceBreakdown = priceBreakdown;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(Integer validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public QuotationStatus getStatus() {
        return status;
    }

    public void setStatus(QuotationStatus status) {
        this.status = status;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public Long getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(Long acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
