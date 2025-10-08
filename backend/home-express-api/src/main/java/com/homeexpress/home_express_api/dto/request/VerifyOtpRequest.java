package com.homeexpress.home_express_api.dto.request;

public class VerifyOtpRequest {
    
    private String email;
    private String code;

    public VerifyOtpRequest() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
