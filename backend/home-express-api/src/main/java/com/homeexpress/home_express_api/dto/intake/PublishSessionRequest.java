package com.homeexpress.home_express_api.dto.intake;

import com.homeexpress.home_express_api.dto.booking.AddressDto;
import com.homeexpress.home_express_api.entity.TimeSlot;
import jakarta.validation.Valid;
import java.time.LocalDate;

/**
 * Payload used by admins to convert an intake session into a real booking.
 */
public class PublishSessionRequest {

    private Integer expiresInHours = 24;

    @Valid
    private AddressDto pickupAddress;

    @Valid
    private AddressDto deliveryAddress;

    private LocalDate preferredDate;

    private TimeSlot preferredTimeSlot;

    private String notes;

    private String specialRequirements;

    public Integer getExpiresInHours() {
        return expiresInHours;
    }

    public void setExpiresInHours(Integer expiresInHours) {
        this.expiresInHours = expiresInHours;
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
}

