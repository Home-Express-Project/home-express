package com.homeexpress.home_express_api.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransportQuotationSummaryResponse {

    private Long quotationId;
    private Long bookingId;
    private String pickupLocation;
    private String deliveryLocation;
    private String preferredDate;
    private double myQuotePrice;
    private String status;
    private int competitorQuotesCount;
    private Double lowestCompetitorPrice;
    private int myRank;
    private LocalDateTime expiresAt;
    private LocalDateTime submittedAt;

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

    public String getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    public double getMyQuotePrice() {
        return myQuotePrice;
    }

    public void setMyQuotePrice(double myQuotePrice) {
        this.myQuotePrice = myQuotePrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCompetitorQuotesCount() {
        return competitorQuotesCount;
    }

    public void setCompetitorQuotesCount(int competitorQuotesCount) {
        this.competitorQuotesCount = competitorQuotesCount;
    }

    public Double getLowestCompetitorPrice() {
        return lowestCompetitorPrice;
    }

    public void setLowestCompetitorPrice(Double lowestCompetitorPrice) {
        this.lowestCompetitorPrice = lowestCompetitorPrice;
    }

    public int getMyRank() {
        return myRank;
    }

    public void setMyRank(int myRank) {
        this.myRank = myRank;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}

