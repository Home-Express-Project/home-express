package com.homeexpress.home_express_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {
    
    private boolean success;
    private String status; // "PENDING", "AWAITING_CUSTOMER", "DEPOSIT_PAID", "FULL_PAID", "FAILED", "CANCELLED"
    private String message;
    private Double amount;
    private String paymentMethod;
    private String paymentDate;
}
