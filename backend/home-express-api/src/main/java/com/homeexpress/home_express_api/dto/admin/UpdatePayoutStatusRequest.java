package com.homeexpress.home_express_api.dto.admin;

import com.homeexpress.home_express_api.entity.PayoutStatus;
import jakarta.validation.constraints.NotNull;

public class UpdatePayoutStatusRequest {

    @NotNull(message = "Status is required")
    private PayoutStatus status;

    private String failureReason;

    private String transactionReference;

    public UpdatePayoutStatusRequest() {
    }

    public PayoutStatus getStatus() {
        return status;
    }

    public void setStatus(PayoutStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
}
