package com.homeexpress.home_express_api.dto.response;

import lombok.Data;

@Data
public class AuthResponse {

    private String token;        // JWT token
    private String accessToken;  // Access token
    private String refreshToken; // Refresh token
    private UserResponse user;   // Thông tin user
    private String message;      // "Login successful"

}
