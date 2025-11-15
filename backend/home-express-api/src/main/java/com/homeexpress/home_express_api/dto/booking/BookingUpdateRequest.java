package com.homeexpress.home_express_api.dto.booking;

import com.homeexpress.home_express_api.entity.BookingStatus;
import com.homeexpress.home_express_api.entity.TimeSlot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class BookingUpdateRequest {

    @Valid
    private AddressDto pickupAddress;

    @Valid
    private AddressDto deliveryAddress;

    private LocalDate preferredDate;

    private TimeSlot preferredTimeSlot;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Size(max = 1000, message = "Special requirements cannot exceed 1000 characters")
    private String specialRequirements;

    private BookingStatus status;

    @Size(max = 1000, message = "Cancellation reason cannot exceed 1000 characters")
    private String cancellationReason;

    public BookingUpdateRequest() {
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

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
