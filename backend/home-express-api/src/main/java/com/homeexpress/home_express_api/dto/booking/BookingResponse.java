package com.homeexpress.home_express_api.dto.booking;

import com.homeexpress.home_express_api.entity.Booking;
import com.homeexpress.home_express_api.entity.BookingStatus;
import com.homeexpress.home_express_api.entity.DistanceSource;
import com.homeexpress.home_express_api.entity.TimeSlot;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingResponse {

    private Long bookingId;
    private Long customerId;
    private Long transportId;
    private AddressDto pickupAddress;
    private AddressDto deliveryAddress;
    private LocalDate preferredDate;
    private TimeSlot preferredTimeSlot;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private BigDecimal distanceKm;
    private DistanceSource distanceSource;
    private LocalDateTime distanceCalculatedAt;
    private BigDecimal estimatedPrice;
    private BigDecimal finalPrice;
    private BookingStatus status;
    private String notes;
    private String specialRequirements;
    private Long cancelledBy;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BookingResponse() {
    }

    public static BookingResponse fromEntity(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setCustomerId(booking.getCustomerId());
        response.setTransportId(booking.getTransportId());
        
        AddressDto pickup = new AddressDto();
        pickup.setAddressLine(booking.getPickupAddress());
        pickup.setProvinceCode(booking.getPickupProvinceCode());
        pickup.setDistrictCode(booking.getPickupDistrictCode());
        pickup.setWardCode(booking.getPickupWardCode());
        pickup.setLat(booking.getPickupLatitude());
        pickup.setLng(booking.getPickupLongitude());
        pickup.setFloor(booking.getPickupFloor());
        pickup.setHasElevator(booking.getPickupHasElevator());
        response.setPickupAddress(pickup);
        
        AddressDto delivery = new AddressDto();
        delivery.setAddressLine(booking.getDeliveryAddress());
        delivery.setProvinceCode(booking.getDeliveryProvinceCode());
        delivery.setDistrictCode(booking.getDeliveryDistrictCode());
        delivery.setWardCode(booking.getDeliveryWardCode());
        delivery.setLat(booking.getDeliveryLatitude());
        delivery.setLng(booking.getDeliveryLongitude());
        delivery.setFloor(booking.getDeliveryFloor());
        delivery.setHasElevator(booking.getDeliveryHasElevator());
        response.setDeliveryAddress(delivery);
        
        response.setPreferredDate(booking.getPreferredDate());
        response.setPreferredTimeSlot(booking.getPreferredTimeSlot());
        response.setActualStartTime(booking.getActualStartTime());
        response.setActualEndTime(booking.getActualEndTime());
        response.setDistanceKm(booking.getDistanceKm());
        response.setDistanceSource(booking.getDistanceSource());
        response.setDistanceCalculatedAt(booking.getDistanceCalculatedAt());
        response.setEstimatedPrice(booking.getEstimatedPrice());
        response.setFinalPrice(booking.getFinalPrice());
        response.setStatus(booking.getStatus());
        response.setNotes(booking.getNotes());
        response.setSpecialRequirements(booking.getSpecialRequirements());
        response.setCancelledBy(booking.getCancelledBy());
        response.setCancellationReason(booking.getCancellationReason());
        response.setCancelledAt(booking.getCancelledAt());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        
        return response;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
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

    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public DistanceSource getDistanceSource() {
        return distanceSource;
    }

    public void setDistanceSource(DistanceSource distanceSource) {
        this.distanceSource = distanceSource;
    }

    public LocalDateTime getDistanceCalculatedAt() {
        return distanceCalculatedAt;
    }

    public void setDistanceCalculatedAt(LocalDateTime distanceCalculatedAt) {
        this.distanceCalculatedAt = distanceCalculatedAt;
    }

    public BigDecimal getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(BigDecimal estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
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

    public Long getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(Long cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
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
