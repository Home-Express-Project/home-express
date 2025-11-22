package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {
    
    // username có thể update
    @Size(min = 3, max = 45, message = "Username phải từ 3-45 ký tự")
    private String username;
    
    // phone cũng có thể đổi
    @Size(min = 10, max = 20, message = "Số điện thoại phải từ 10-20 ký tự")
    private String phone;
    
    // email cũng cho phép update
    @Email(message = "Email không hợp lệ")
    private String email;
    
    private String avatar;
    
    // admin có thể active/deactive user
    private Boolean isActive;

    // Constructors
    public UpdateUserRequest() {
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
