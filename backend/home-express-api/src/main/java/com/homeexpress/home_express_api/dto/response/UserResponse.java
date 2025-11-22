package com.homeexpress.home_express_api.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserResponse {

    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String avatar;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime createdAt;

}
