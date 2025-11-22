package com.homeexpress.home_express_api.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {

    private String username;
    private String email;
    private String phone;
    private String password;
    private String role; // "customer", "transport", "manager"
    private String fullName;
    private String address;
    private String city;
    private String companyName;
    private String businessLicenseNumber;

}
