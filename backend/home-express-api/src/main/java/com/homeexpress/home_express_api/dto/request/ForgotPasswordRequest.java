package com.homeexpress.home_express_api.dto.request;

public class ForgotPasswordRequest {
    
    private String email;

    public ForgotPasswordRequest() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
