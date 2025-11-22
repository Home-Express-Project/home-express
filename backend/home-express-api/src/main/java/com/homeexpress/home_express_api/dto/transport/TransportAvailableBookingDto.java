package com.homeexpress.home_express_api.dto.transport;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransportAvailableBookingDto {

    private Long bookingId;
    private String pickupLocation;
    private String deliveryLocation;
    private Double distanceKm;
    private Double distanceFromMe;
    private Integer itemsCount;
    private Double totalWeight;
    private Boolean hasFragileItems;
    private String preferredDate;
    private String preferredTimeSlot;
    private Long estimatedPrice;
    private Integer quotationsCount;
    private Boolean hasQuoted;
    private Long suggestedPrice;
    private String expiresAt;
    private String notifiedAt;

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Double getDistanceFromMe() {
        return distanceFromMe;
    }

    public void setDistanceFromMe(Double distanceFromMe) {
        this.distanceFromMe = distanceFromMe;
    }

    public Integer getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(Integer itemsCount) {
        this.itemsCount = itemsCount;
    }

    public Double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(Double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public Boolean getHasFragileItems() {
        return hasFragileItems;
    }

    public void setHasFragileItems(Boolean hasFragileItems) {
        this.hasFragileItems = hasFragileItems;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    public String getPreferredTimeSlot() {
        return preferredTimeSlot;
    }

    public void setPreferredTimeSlot(String preferredTimeSlot) {
        this.preferredTimeSlot = preferredTimeSlot;
    }

    public Long getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(Long estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public Integer getQuotationsCount() {
        return quotationsCount;
    }

    public void setQuotationsCount(Integer quotationsCount) {
        this.quotationsCount = quotationsCount;
    }

    public Boolean getHasQuoted() {
        return hasQuoted;
    }

    public void setHasQuoted(Boolean hasQuoted) {
        this.hasQuoted = hasQuoted;
    }

    public Long getSuggestedPrice() {
        return suggestedPrice;
    }

    public void setSuggestedPrice(Long suggestedPrice) {
        this.suggestedPrice = suggestedPrice;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(String notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
}

