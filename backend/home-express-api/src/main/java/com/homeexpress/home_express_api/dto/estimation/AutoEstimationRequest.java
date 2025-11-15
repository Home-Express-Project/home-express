package com.homeexpress.home_express_api.dto.estimation;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AutoEstimationRequest {

    @NotBlank
    @JsonProperty("pickup_address")
    private String pickupAddress;

    @NotBlank
    @JsonProperty("delivery_address")
    private String deliveryAddress;

    @Valid
    @NotEmpty
    private List<Item> items = new ArrayList<>();

    @JsonProperty("pickup_floor")
    private Integer pickupFloor;

    @JsonProperty("delivery_floor")
    private Integer deliveryFloor;

    @JsonProperty("has_elevator_pickup")
    private Boolean hasElevatorPickup;

    @JsonProperty("has_elevator_delivery")
    private Boolean hasElevatorDelivery;

    @JsonProperty("pickup_datetime")
    private String pickupDatetime;

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Integer getPickupFloor() {
        return pickupFloor;
    }

    public void setPickupFloor(Integer pickupFloor) {
        this.pickupFloor = pickupFloor;
    }

    public Integer getDeliveryFloor() {
        return deliveryFloor;
    }

    public void setDeliveryFloor(Integer deliveryFloor) {
        this.deliveryFloor = deliveryFloor;
    }

    public Boolean getHasElevatorPickup() {
        return hasElevatorPickup;
    }

    public void setHasElevatorPickup(Boolean hasElevatorPickup) {
        this.hasElevatorPickup = hasElevatorPickup;
    }

    public Boolean getHasElevatorDelivery() {
        return hasElevatorDelivery;
    }

    public void setHasElevatorDelivery(Boolean hasElevatorDelivery) {
        this.hasElevatorDelivery = hasElevatorDelivery;
    }

    public String getPickupDatetime() {
        return pickupDatetime;
    }

    public void setPickupDatetime(String pickupDatetime) {
        this.pickupDatetime = pickupDatetime;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        @NotNull
        private Long categoryId;

        @JsonProperty("name")
        private String name;

        private Integer quantity = 1;

        private Double weight;

        @JsonProperty("isFragile")
        private Boolean fragile;

        private Boolean requiresDisassembly;

        private Boolean requiresPackaging;

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

        public Integer getQuantity() {
            return quantity != null && quantity > 0 ? quantity : 1;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        public Boolean getFragile() {
            return fragile;
        }

        public void setFragile(Boolean fragile) {
            this.fragile = fragile;
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
    }
}
