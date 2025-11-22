package com.homeexpress.home_express_api.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public class QuoteEstimateResponse {
    private Long transportId;
    private String transportName;
    private String transportAvatar;
    private Double averageRating;
    private Integer completedBookings;
    
    private Long vehicleId;
    private String vehicleModel;
    private String vehicleType;
    private String licensePlate; // Obfuscated like "29A-123.**"
    
    private BigDecimal totalPrice;
    private BigDecimal transportFee;
    private BigDecimal itemHandlingFee;
    
    private Boolean isRecommended; // Flag for top ranked
    private String matchReason; // e.g., "Best Price", "Top Rated"
    
    private Map<String, Object> priceBreakdown; // Detailed breakdown

    // Getters and Setters
    public QuoteEstimateResponse() {}

    public Long getTransportId() { return transportId; }
    public void setTransportId(Long transportId) { this.transportId = transportId; }

    public String getTransportName() { return transportName; }
    public void setTransportName(String transportName) { this.transportName = transportName; }

    public String getTransportAvatar() { return transportAvatar; }
    public void setTransportAvatar(String transportAvatar) { this.transportAvatar = transportAvatar; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getCompletedBookings() { return completedBookings; }
    public void setCompletedBookings(Integer completedBookings) { this.completedBookings = completedBookings; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BigDecimal getTransportFee() { return transportFee; }
    public void setTransportFee(BigDecimal transportFee) { this.transportFee = transportFee; }

    public BigDecimal getItemHandlingFee() { return itemHandlingFee; }
    public void setItemHandlingFee(BigDecimal itemHandlingFee) { this.itemHandlingFee = itemHandlingFee; }

    public Boolean getIsRecommended() { return isRecommended; }
    public void setIsRecommended(Boolean isRecommended) { this.isRecommended = isRecommended; }

    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }

    public Map<String, Object> getPriceBreakdown() { return priceBreakdown; }
    public void setPriceBreakdown(Map<String, Object> priceBreakdown) { this.priceBreakdown = priceBreakdown; }
}
