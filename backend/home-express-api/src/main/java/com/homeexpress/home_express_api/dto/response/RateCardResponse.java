package com.homeexpress.home_express_api.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.homeexpress.home_express_api.entity.RateCard;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RateCardResponse {

    private Long rateCardId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal basePrice;
    private BigDecimal pricePerKm;
    private BigDecimal pricePerHour;
    private BigDecimal minimumCharge;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean isActive;
    private Map<String, BigDecimal> additionalRules;

    public static RateCardResponse fromEntity(RateCard entity, String categoryName, Map<String, BigDecimal> additionalRules) {
        RateCardResponse response = new RateCardResponse();
        response.setRateCardId(entity.getRateCardId());
        response.setCategoryId(entity.getCategoryId());
        response.setCategoryName(categoryName);
        response.setBasePrice(entity.getBasePrice());
        response.setPricePerKm(entity.getPricePerKm());
        response.setPricePerHour(entity.getPricePerHour());
        response.setMinimumCharge(entity.getMinimumCharge());
        response.setValidFrom(entity.getValidFrom());
        response.setValidUntil(entity.getValidUntil());
        response.setIsActive(entity.getIsActive());
        response.setAdditionalRules(additionalRules);
        return response;
    }

    public Long getRateCardId() {
        return rateCardId;
    }

    public void setRateCardId(Long rateCardId) {
        this.rateCardId = rateCardId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getPricePerKm() {
        return pricePerKm;
    }

    public void setPricePerKm(BigDecimal pricePerKm) {
        this.pricePerKm = pricePerKm;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public BigDecimal getMinimumCharge() {
        return minimumCharge;
    }

    public void setMinimumCharge(BigDecimal minimumCharge) {
        this.minimumCharge = minimumCharge;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Map<String, BigDecimal> getAdditionalRules() {
        return additionalRules;
    }

    public void setAdditionalRules(Map<String, BigDecimal> additionalRules) {
        this.additionalRules = additionalRules;
    }
}

