package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class CategoryRequest {
    
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    
    @Size(max = 100, message = "English name must not exceed 100 characters")
    private String nameEn;
    
    private String description;
    
    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;
    
    private BigDecimal defaultWeightKg;
    private BigDecimal defaultVolumeM3;
    private BigDecimal defaultLengthCm;
    private BigDecimal defaultWidthCm;
    private BigDecimal defaultHeightCm;
    private Boolean isFragileDefault;
    private Boolean requiresDisassemblyDefault;
    private Integer displayOrder;
    private Boolean isActive;

    public CategoryRequest() {
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

    public Boolean getIsFragileDefault() {
        return isFragileDefault;
    }

    public void setIsFragileDefault(Boolean isFragileDefault) {
        this.isFragileDefault = isFragileDefault;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
