package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TransportVerificationRequest {
    
    @NotNull(message = "Verification decision is required")
    private Boolean isVerified;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String verificationNotes;

    public TransportVerificationRequest() {
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }
}
