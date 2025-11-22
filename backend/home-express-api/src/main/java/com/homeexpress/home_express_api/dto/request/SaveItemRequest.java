package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * DTO for creating/updating a saved item
 */
public class SaveItemRequest {

    @NotBlank(message = "Item name is required")
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

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity = 1;

    @JsonProperty("isFragile")
    private Boolean isFragile = false;

    @JsonProperty("requiresDisassembly")
    private Boolean requiresDisassembly = false;

    @JsonProperty("requiresPackaging")
    private Boolean requiresPackaging = false;

    private String notes;

    private String metadata;

    // Getters and Setters

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
}
