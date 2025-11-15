package com.homeexpress.home_express_api.controller.transport;

import com.homeexpress.home_express_api.dto.request.UpdateTransportSettingsRequest;
import com.homeexpress.home_express_api.dto.response.TransportSettingsResponse;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.service.TransportSettingsService;
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
@RequestMapping("/api/v1/transport/settings")
public class TransportSettingsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransportSettingsService transportSettingsService;

    @GetMapping
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<?> getSettings(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only transport accounts can access this resource"));
        }

        TransportSettingsResponse response = transportSettingsService.getSettings(user.getUserId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
        ));
    }

    @PutMapping
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<?> updateSettings(@Valid @RequestBody UpdateTransportSettingsRequest request,
                                            Authentication authentication) {
        User user = resolveUser(authentication);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only transport accounts can update these settings"));
        }

        TransportSettingsResponse updated = transportSettingsService.updateSettings(user.getUserId(), request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Transport settings updated successfully",
                "data", updated
        ));
    }

    private User resolveUser(Authentication authentication) {
        return AuthenticationUtils.getUser(authentication, userRepository);
    }
}
