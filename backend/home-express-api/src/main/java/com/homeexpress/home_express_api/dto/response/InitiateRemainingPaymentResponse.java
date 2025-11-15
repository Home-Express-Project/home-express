package com.homeexpress.home_express_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateRemainingPaymentResponse {
    
    private boolean success;
    private String paymentId;
    private String paymentUrl;
    private String tipPaymentId;
    private Long bookingId;
    private String message;
    private Long remainingAmountVnd;
    private Long tipAmountVnd;
    private Long totalAmountVnd;
}
