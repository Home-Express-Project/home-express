package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @NotBlank
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "name_en", length = 100)
    private String nameEn;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "default_weight_kg")
    private BigDecimal defaultWeightKg;

    @Column(name = "default_volume_m3")
    private BigDecimal defaultVolumeM3;

    @Column(name = "default_length_cm")
    private BigDecimal defaultLengthCm;

    @Column(name = "default_width_cm")
    private BigDecimal defaultWidthCm;

    @Column(name = "default_height_cm")
    private BigDecimal defaultHeightCm;

    @NotNull
    @Column(name = "is_fragile_default", nullable = false)
    private Boolean isFragileDefault = false;

    @NotNull
    @Column(name = "requires_disassembly_default", nullable = false)
    private Boolean requiresDisassemblyDefault = false;

    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Category() {}

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
