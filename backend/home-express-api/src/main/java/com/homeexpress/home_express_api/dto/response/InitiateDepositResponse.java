package com.homeexpress.home_express_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateDepositResponse {
    
    private boolean success;
    private String paymentId;
    private String paymentUrl;
    private Long bookingId;
    private String message;
    private Double depositAmount;
}
