package com.homeexpress.home_express_api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.homeexpress.home_express_api.entity.Category;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransportCategoryResponse {

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("name_en")
    private String nameEn;

    @JsonProperty("description")
    private String description;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("default_weight_kg")
    private BigDecimal defaultWeightKg;

    @JsonProperty("default_volume_m3")
    private BigDecimal defaultVolumeM3;

    @JsonProperty("default_length_cm")
    private BigDecimal defaultLengthCm;

    @JsonProperty("default_width_cm")
    private BigDecimal defaultWidthCm;

    @JsonProperty("default_height_cm")
    private BigDecimal defaultHeightCm;

    @JsonProperty("is_fragile_default")
    private Boolean fragileDefault;

    @JsonProperty("requires_disassembly_default")
    private Boolean requiresDisassemblyDefault;

    @JsonProperty("display_order")
    private Integer displayOrder;

    @JsonProperty("is_active")
    private Boolean active;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("sizes")
    private List<TransportSizeResponse> sizes = new ArrayList<>();

    public static TransportCategoryResponse fromEntity(
            Category category,
            List<TransportSizeResponse> sizeResponses) {
        TransportCategoryResponse response = new TransportCategoryResponse();
        response.setCategoryId(category.getCategoryId());
        response.setName(category.getName());
        response.setNameEn(category.getNameEn());
        response.setDescription(category.getDescription());
        response.setIcon(category.getIcon());
        response.setDefaultWeightKg(category.getDefaultWeightKg());
        response.setDefaultVolumeM3(category.getDefaultVolumeM3());
        response.setDefaultLengthCm(category.getDefaultLengthCm());
        response.setDefaultWidthCm(category.getDefaultWidthCm());
        response.setDefaultHeightCm(category.getDefaultHeightCm());
        response.setFragileDefault(category.getIsFragileDefault());
        response.setRequiresDisassemblyDefault(category.getRequiresDisassemblyDefault());
        response.setDisplayOrder(category.getDisplayOrder());
        response.setActive(category.getIsActive());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        response.setSizes(sizeResponses);
        return response;
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

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public BigDecimal getDefaultWeightKg() {
        return defaultWeightKg;
    }

    public void setDefaultWeightKg(BigDecimal defaultWeightKg) {
        this.defaultWeightKg = defaultWeightKg;
    }

    public BigDecimal getDefaultVolumeM3() {
        return defaultVolumeM3;
    }

    public void setDefaultVolumeM3(BigDecimal defaultVolumeM3) {
        this.defaultVolumeM3 = defaultVolumeM3;
    }

    public BigDecimal getDefaultLengthCm() {
        return defaultLengthCm;
    }

    public void setDefaultLengthCm(BigDecimal defaultLengthCm) {
        this.defaultLengthCm = defaultLengthCm;
    }

    public BigDecimal getDefaultWidthCm() {
        return defaultWidthCm;
    }

    public void setDefaultWidthCm(BigDecimal defaultWidthCm) {
        this.defaultWidthCm = defaultWidthCm;
    }

    public BigDecimal getDefaultHeightCm() {
        return defaultHeightCm;
    }

    public void setDefaultHeightCm(BigDecimal defaultHeightCm) {
        this.defaultHeightCm = defaultHeightCm;
    }

    public Boolean getFragileDefault() {
        return fragileDefault;
    }

    public void setFragileDefault(Boolean fragileDefault) {
        this.fragileDefault = fragileDefault;
    }

    public Boolean getRequiresDisassemblyDefault() {
        return requiresDisassemblyDefault;
    }

    public void setRequiresDisassemblyDefault(Boolean requiresDisassemblyDefault) {
        this.requiresDisassemblyDefault = requiresDisassemblyDefault;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public List<TransportSizeResponse> getSizes() {
        return sizes;
    }

    public void setSizes(List<TransportSizeResponse> sizes) {
        this.sizes = sizes;
    }
}
