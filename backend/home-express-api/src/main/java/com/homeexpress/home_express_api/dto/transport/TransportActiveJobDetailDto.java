package com.homeexpress.home_express_api.dto.transport;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.ArrayList;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransportActiveJobDetailDto {

    private Long bookingId;
    private String status;
    private String preferredDate;
    private String preferredTimeSlot;
    private Long finalPrice;
    private Double distanceKm;

    private String pickupAddress;
    private String pickupContactName;
    private String pickupContactPhone;

    private String deliveryAddress;
    private String deliveryContactName;
    private String deliveryContactPhone;

    private List<JobItem> items = new ArrayList<>();
    private List<JobStatusHistory> statusHistory = new ArrayList<>();

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    public String getPreferredTimeSlot() {
        return preferredTimeSlot;
    }

    public void setPreferredTimeSlot(String preferredTimeSlot) {
        this.preferredTimeSlot = preferredTimeSlot;
    }

    public Long getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Long finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getPickupContactName() {
        return pickupContactName;
    }

    public void setPickupContactName(String pickupContactName) {
        this.pickupContactName = pickupContactName;
    }

    public String getPickupContactPhone() {
        return pickupContactPhone;
    }

    public void setPickupContactPhone(String pickupContactPhone) {
        this.pickupContactPhone = pickupContactPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryContactName() {
        return deliveryContactName;
    }

    public void setDeliveryContactName(String deliveryContactName) {
        this.deliveryContactName = deliveryContactName;
    }

    public String getDeliveryContactPhone() {
        return deliveryContactPhone;
    }

    public void setDeliveryContactPhone(String deliveryContactPhone) {
        this.deliveryContactPhone = deliveryContactPhone;
    }

    public List<JobItem> getItems() {
        return items;
    }

    public void setItems(List<JobItem> items) {
        this.items = items;
    }

    public List<JobStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<JobStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class JobItem {
        private String name;
        private Integer quantity;
        private Double weight;
        private Boolean isFragile;
        private Boolean requiresDisassembly;
        private Boolean requiresPackaging;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getQuantity() {
            return quantity;
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
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class JobStatusHistory {
        private String status;
        private String changedAt;
        private String changedByRole;
        private String note;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getChangedAt() {
            return changedAt;
        }

        public void setChangedAt(String changedAt) {
            this.changedAt = changedAt;
        }

        public String getChangedByRole() {
            return changedByRole;
        }

        public void setChangedByRole(String changedByRole) {
            this.changedByRole = changedByRole;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }
}

