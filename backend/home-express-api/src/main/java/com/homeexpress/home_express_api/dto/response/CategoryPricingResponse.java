package com.homeexpress.home_express_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.homeexpress.home_express_api.entity.CategoryPricing;

public class CategoryPricingResponse {

    private Long categoryPricingId;
    private Long transportId;
    private String transportCompanyName;
    private Long categoryId;
    private String categoryName;
    private Long sizeId;
    private String sizeName;
    private BigDecimal pricePerUnitVnd;
    private BigDecimal fragileMultiplier;
    private BigDecimal disassemblyMultiplier;
    private BigDecimal heavyMultiplier;
    private BigDecimal heavyThresholdKg;
    private Boolean isActive;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CategoryPricingResponse() {
    }

    public static CategoryPricingResponse fromEntity(CategoryPricing entity) {
        CategoryPricingResponse response = new CategoryPricingResponse();
        response.setCategoryPricingId(entity.getCategoryPricingId());
        response.setTransportId(entity.getTransport().getTransportId());
        response.setTransportCompanyName(entity.getTransport().getCompanyName());
        response.setCategoryId(entity.getCategory().getCategoryId());
        response.setCategoryName(entity.getCategory().getName());

        if (entity.getSize() != null) {
            response.setSizeId(entity.getSize().getSizeId());
            response.setSizeName(entity.getSize().getName());
        }

        response.setPricePerUnitVnd(entity.getPricePerUnitVnd());
        response.setFragileMultiplier(entity.getFragileMultiplier());
        response.setDisassemblyMultiplier(entity.getDisassemblyMultiplier());
        response.setHeavyMultiplier(entity.getHeavyMultiplier());
        response.setHeavyThresholdKg(entity.getHeavyThresholdKg());
        response.setIsActive(entity.getIsActive());
        response.setValidFrom(entity.getValidFrom());
        response.setValidTo(entity.getValidTo());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        return response;
    }

    public Long getCategoryPricingId() {
        return categoryPricingId;
    }

    public void setCategoryPricingId(Long categoryPricingId) {
        this.categoryPricingId = categoryPricingId;
    }

    /**
     * Alias for getCategoryPricingId() for backward compatibility Used by
     * controllers that return Map.of("pricingId", response.getPricingId())
     */
    public Long getPricingId() {
        return this.categoryPricingId;
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

    public Long getSizeId() {
        return sizeId;
    }

    public void setSizeId(Long sizeId) {
        this.sizeId = sizeId;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public BigDecimal getPricePerUnitVnd() {
        return pricePerUnitVnd;
    }

    public void setPricePerUnitVnd(BigDecimal pricePerUnitVnd) {
        this.pricePerUnitVnd = pricePerUnitVnd;
    }

    public BigDecimal getFragileMultiplier() {
        return fragileMultiplier;
    }

    public void setFragileMultiplier(BigDecimal fragileMultiplier) {
        this.fragileMultiplier = fragileMultiplier;
    }

    public BigDecimal getDisassemblyMultiplier() {
        return disassemblyMultiplier;
    }

    public void setDisassemblyMultiplier(BigDecimal disassemblyMultiplier) {
        this.disassemblyMultiplier = disassemblyMultiplier;
    }

    public BigDecimal getHeavyMultiplier() {
        return heavyMultiplier;
    }

    public void setHeavyMultiplier(BigDecimal heavyMultiplier) {
        this.heavyMultiplier = heavyMultiplier;
    }

    public BigDecimal getHeavyThresholdKg() {
        return heavyThresholdKg;
    }

    public void setHeavyThresholdKg(BigDecimal heavyThresholdKg) {
        this.heavyThresholdKg = heavyThresholdKg;
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
