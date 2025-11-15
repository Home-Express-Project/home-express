package com.homeexpress.home_express_api.dto.admin;

import com.homeexpress.home_express_api.entity.SettlementStatus;
import jakarta.validation.constraints.NotNull;

public class SettlementReviewRequest {

    @NotNull(message = "Status is required")
    private SettlementStatus status;

    private String reason;

    public SettlementReviewRequest() {
    }

    public SettlementStatus getStatus() {
        return status;
    }

    public void setStatus(SettlementStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
