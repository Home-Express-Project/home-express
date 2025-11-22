package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiateDepositRequest {
    
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Payment method is required")
    private PaymentMethodRequest method; // "cash" or "bank"

    // Optional return URLs for payment gateways
    private String returnUrl;
    private String cancelUrl;
}
