package com.homeexpress.home_express_api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for returning saved item information
 */
public class SavedItemResponse {

    @JsonProperty("savedItemId")
    private Long savedItemId;

    private String name;

    private String brand;

    private String model;

    @JsonProperty("categoryId")
    private Long categoryId;

    private String size;

    @JsonProperty("weightKg")
    private BigDecimal weightKg;

    private String dimensions;

    @JsonProperty("declaredValueVnd")
    private BigDecimal declaredValueVnd;

    private Integer quantity;

    @JsonProperty("isFragile")
    private Boolean isFragile;

    @JsonProperty("requiresDisassembly")
    private Boolean requiresDisassembly;

    @JsonProperty("requiresPackaging")
    private Boolean requiresPackaging;

    private String notes;

    private String metadata;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    // Constructors
    public SavedItemResponse() {
    }

    public SavedItemResponse(
            Long savedItemId, String name, String brand, String model,
            Long categoryId, String size, BigDecimal weightKg, String dimensions,
            BigDecimal declaredValueVnd, Integer quantity, Boolean isFragile,
            Boolean requiresDisassembly, Boolean requiresPackaging,
            String notes, String metadata, LocalDateTime createdAt) {
        this.savedItemId = savedItemId;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.categoryId = categoryId;
        this.size = size;
        this.weightKg = weightKg;
        this.dimensions = dimensions;
        this.declaredValueVnd = declaredValueVnd;
        this.quantity = quantity;
        this.isFragile = isFragile;
        this.requiresDisassembly = requiresDisassembly;
        this.requiresPackaging = requiresPackaging;
        this.notes = notes;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public Long getSavedItemId() {
        return savedItemId;
    }

    public void setSavedItemId(Long savedItemId) {
        this.savedItemId = savedItemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public BigDecimal getDeclaredValueVnd() {
        return declaredValueVnd;
    }

    public void setDeclaredValueVnd(BigDecimal declaredValueVnd) {
        this.declaredValueVnd = declaredValueVnd;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Boolean getIsFragile() {
        return isFragile;
    }

    public void setIsFragile(Boolean isFragile) {
        this.isFragile = isFragile;
    }

    public Boolean getRequiresDisassembly() {
        return requiresDisassembly;
    }

    public void setRequiresDisassembly(Boolean requiresDisassembly) {
        this.requiresDisassembly = requiresDisassembly;
    }

    public Boolean getRequiresPackaging() {
        return requiresPackaging;
    }

    public void setRequiresPackaging(Boolean requiresPackaging) {
        this.requiresPackaging = requiresPackaging;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
