package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiateRemainingPaymentRequest {
    
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Payment method is required")
    private PaymentMethodRequest method; // "cash" or "bank"

    @PositiveOrZero(message = "Tip amount must be zero or positive")
    private Long tipAmountVnd; // Optional tip in VND

    // Optional return URLs for payment gateways
    private String returnUrl;
    private String cancelUrl;
}

