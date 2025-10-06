package com.homeexpress.home_express_api.dto.response;

public class AuthResponse {

    private String token;        // JWT token
    private UserResponse user;   // Thông tin user
    private String message;      // "Login successful"

    // Constructor
    public AuthResponse() {
    }
    // Getters and Setters

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
