package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class QuoteCalculationRequest {

    @NotNull(message = "Intake Session ID is required")
    private Long intakeSessionId;

    // We can include location details if not already in the Intake/Booking
    // For now, we assume we are calculating for a specific Intake Session which might be linked to a potential booking
    
    private Long bookingId; // Optional: if calculating for an existing booking
    
    private Double distanceKm; // Optional: override distance

    public Long getIntakeSessionId() {
        return intakeSessionId;
    }

    public void setIntakeSessionId(Long intakeSessionId) {
        this.intakeSessionId = intakeSessionId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
