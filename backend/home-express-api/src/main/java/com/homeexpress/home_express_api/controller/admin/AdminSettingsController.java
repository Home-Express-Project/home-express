package com.homeexpress.home_express_api.controller.admin;

import com.homeexpress.home_express_api.dto.request.UpdateAdminSettingsRequest;
import com.homeexpress.home_express_api.dto.response.AdminSettingsResponse;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.service.AdminSettingsService;
import com.homeexpress.home_express_api.util.AuthenticationUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/settings")
public class AdminSettingsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminSettingsService adminSettingsService;

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getSettings(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only managers can access admin settings"));
        }

        AdminSettingsResponse response = adminSettingsService.getSettings(user.getUserId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
        ));
    }

    @PutMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> updateSettings(@Valid @RequestBody UpdateAdminSettingsRequest request,
                                            Authentication authentication) {
        User user = resolveUser(authentication);
        if (user.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only managers can update admin settings"));
        }

        AdminSettingsResponse response = adminSettingsService.updateSettings(user.getUserId(), request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Admin settings updated successfully",
                "data", response
        ));
    }

    private User resolveUser(Authentication authentication) {
        return AuthenticationUtils.getUser(authentication, userRepository);
    }
}
