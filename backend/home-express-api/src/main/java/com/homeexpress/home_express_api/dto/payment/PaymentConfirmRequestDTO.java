package com.homeexpress.home_express_api.dto.payment;

import jakarta.validation.constraints.NotNull;

public class PaymentConfirmRequestDTO {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    @NotNull(message = "Transaction ID is required")
    private String transactionId;

    private String gatewayResponse;

    public PaymentConfirmRequestDTO() {
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }

    public void setGatewayResponse(String gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }
}
