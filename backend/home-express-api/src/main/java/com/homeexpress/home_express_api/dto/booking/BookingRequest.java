package com.homeexpress.home_express_api.dto.booking;

import com.homeexpress.home_express_api.entity.TimeSlot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BookingRequest {

    @NotNull(message = "Pickup address is required")
    @Valid
    private AddressDto pickupAddress;

    @NotNull(message = "Delivery address is required")
    @Valid
    private AddressDto deliveryAddress;

    @NotNull(message = "Items are required")
    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<ItemDto> items;

    @NotNull(message = "Preferred date is required")
    @Future(message = "Preferred date must be in the future")
    private LocalDate preferredDate;

    private TimeSlot preferredTimeSlot;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Size(max = 1000, message = "Special requirements cannot exceed 1000 characters")
    private String specialRequirements;

    private Long transportId;

    private BigDecimal estimatedPrice;

    public BookingRequest() {
    }

    public AddressDto getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(AddressDto pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public AddressDto getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(AddressDto deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public LocalDate getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(LocalDate preferredDate) {
        this.preferredDate = preferredDate;
    }

    public TimeSlot getPreferredTimeSlot() {
        return preferredTimeSlot;
    }

    public void setPreferredTimeSlot(TimeSlot preferredTimeSlot) {
        this.preferredTimeSlot = preferredTimeSlot;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSpecialRequirements() {
        return specialRequirements;
    }

    public void setSpecialRequirements(String specialRequirements) {
        this.specialRequirements = specialRequirements;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public BigDecimal getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(BigDecimal estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public static class ItemDto {
        @NotNull(message = "Category ID is required")
        private Long categoryId;

        @NotNull(message = "Item name is required")
        @Size(min = 1, max = 255, message = "Item name must be between 1 and 255 characters")
        private String name;

        private String brand;
        private String model;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        private BigDecimal weight;
        private BigDecimal declaredValueVnd;
        private Boolean isFragile;
        private Boolean requiresDisassembly;
        private Boolean requiresPackaging;
        private List<String> imageUrls;

        public ItemDto() {}

        // Getters and Setters
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }
        
        public BigDecimal getDeclaredValueVnd() { return declaredValueVnd; }
        public void setDeclaredValueVnd(BigDecimal declaredValueVnd) { this.declaredValueVnd = declaredValueVnd; }
        
        public Boolean getIsFragile() { return isFragile; }
        public void setIsFragile(Boolean isFragile) { this.isFragile = isFragile; }
        
        public Boolean getRequiresDisassembly() { return requiresDisassembly; }
        public void setRequiresDisassembly(Boolean requiresDisassembly) { this.requiresDisassembly = requiresDisassembly; }
        
        public Boolean getRequiresPackaging() { return requiresPackaging; }
        public void setRequiresPackaging(Boolean requiresPackaging) { this.requiresPackaging = requiresPackaging; }
        
        public List<String> getImageUrls() { return imageUrls; }
        public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    }
}
