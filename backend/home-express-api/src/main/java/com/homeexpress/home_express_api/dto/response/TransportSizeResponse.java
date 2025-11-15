package com.homeexpress.home_express_api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.homeexpress.home_express_api.entity.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransportSizeResponse {

    @JsonProperty("size_id")
    private Long sizeId;

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("weight_kg")
    private BigDecimal weightKg;

    @JsonProperty("height_cm")
    private BigDecimal heightCm;

    @JsonProperty("width_cm")
    private BigDecimal widthCm;

    @JsonProperty("depth_cm")
    private BigDecimal depthCm;

    @JsonProperty("price_multiplier")
    private BigDecimal priceMultiplier;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static TransportSizeResponse fromEntity(Size size) {
        TransportSizeResponse response = new TransportSizeResponse();
        response.setSizeId(size.getSizeId());
        response.setCategoryId(size.getCategory().getCategoryId());
        response.setName(size.getName());
        response.setWeightKg(size.getWeightKg());
        response.setHeightCm(size.getHeightCm());
        response.setWidthCm(size.getWidthCm());
        response.setDepthCm(size.getDepthCm());
        response.setPriceMultiplier(size.getPriceMultiplier());
        response.setCreatedAt(size.getCreatedAt());
        return response;
    }

    public Long getSizeId() {
        return sizeId;
    }

    public void setSizeId(Long sizeId) {
        this.sizeId = sizeId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public BigDecimal getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(BigDecimal widthCm) {
        this.widthCm = widthCm;
    }

    public BigDecimal getDepthCm() {
        return depthCm;
    }

    public void setDepthCm(BigDecimal depthCm) {
        this.depthCm = depthCm;
    }

    public BigDecimal getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setPriceMultiplier(BigDecimal priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
