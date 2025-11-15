package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotNull;

public class UserActivationRequest {
    
    @NotNull(message = "isActive is required")
    private Boolean isActive;
    
    private String reason;

    public UserActivationRequest() {
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
