package com.homeexpress.home_express_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.homeexpress.home_express_api.dto.request.ChangePasswordRequest;
import com.homeexpress.home_express_api.dto.request.UpdateProfileRequest;
import com.homeexpress.home_express_api.dto.request.UpdateUserRequest;
import com.homeexpress.home_express_api.dto.response.UserListResponse;
import com.homeexpress.home_express_api.dto.response.UserResponse;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.dto.response.ProfileResponse;
import com.homeexpress.home_express_api.service.UserService;
import com.homeexpress.home_express_api.service.FileStorageService;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.util.AuthenticationUtils;

import java.util.Map;
import java.util.HashMap;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // Lấy danh sách users - chỉ MANAGER được xem
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserListResponse> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UserListResponse response = userService.getAllUsers(role, page, size);
        return ResponseEntity.ok(response);
    }

    // Lấy thông tin 1 user
    // MANAGER xem được tất cả, user thường chỉ xem được của mình
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or #id == authentication.principal.userId")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    // Update user
    // MANAGER update được tất cả, user thường chỉ update được của mình
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or #id == authentication.principal.userId")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    // Xóa user - chỉ MANAGER được xóa
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        ProfileResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateCurrentUserProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = resolveUserId(authentication);
        ProfileResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = resolveUserId(authentication);
        userService.changePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            Authentication authentication,
            @RequestParam("avatar") MultipartFile file) {
        try {
            Long userId = resolveUserId(authentication);
            String avatarUrl = fileStorageService.saveAvatar(file);
            
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setAvatar(avatarUrl);
            userService.updateProfile(userId, request);
            
            Map<String, String> response = new HashMap<>();
            response.put("avatar_url", avatarUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Long resolveUserId(Authentication authentication) {
        Long id = AuthenticationUtils.getUserId(authentication);
        if (id != null) {
            return id;
        }
        return AuthenticationUtils.getUser(authentication, userRepository).getUserId();
    }
}
