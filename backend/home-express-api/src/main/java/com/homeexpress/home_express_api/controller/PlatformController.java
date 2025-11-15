package com.homeexpress.home_express_api.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.homeexpress.home_express_api.dto.response.UserListResponse;
import com.homeexpress.home_express_api.dto.response.UserResponse;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.service.DashboardService;
import com.homeexpress.home_express_api.service.UserService;

@RestController
@RequestMapping("/api/v1/admin/platform")
public class PlatformController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getPlatformDashboard() {
        Map<String, Object> stats = dashboardService.getPlatformStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserListResponse> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        UserListResponse response = userService.getAllUsers(role, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> activateUser(
            @PathVariable Long id,
            Authentication authentication) {
        
        UserResponse response = userService.updateUser(id, 
            new com.homeexpress.home_express_api.dto.request.UpdateUserRequest() {{
                setIsActive(true);
            }});
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> deactivateUser(
            @PathVariable Long id,
            Authentication authentication) {
        
        UserResponse response = userService.updateUser(id, 
            new com.homeexpress.home_express_api.dto.request.UpdateUserRequest() {{
                setIsActive(false);
            }});
        
        return ResponseEntity.ok(response);
    }
}
