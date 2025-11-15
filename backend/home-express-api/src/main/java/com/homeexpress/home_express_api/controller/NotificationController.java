package com.homeexpress.home_express_api.controller;

import com.homeexpress.home_express_api.dto.request.MarkAsReadRequest;
import com.homeexpress.home_express_api.dto.request.NotificationPreferenceRequest;
import com.homeexpress.home_express_api.dto.response.NotificationPreferenceResponse;
import com.homeexpress.home_express_api.dto.response.NotificationResponse;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.service.NotificationPreferenceService;
import com.homeexpress.home_express_api.service.NotificationService;
import com.homeexpress.home_express_api.util.AuthenticationUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;
    private final UserRepository userRepository;

    public NotificationController(
            NotificationService notificationService,
            NotificationPreferenceService preferenceService,
            UserRepository userRepository) {
        this.notificationService = notificationService;
        this.preferenceService = preferenceService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            Authentication authentication,
            @RequestParam(required = false) Boolean isRead,
            Pageable pageable) {
        User user = resolveAuthenticatedUser(authentication);
        Page<NotificationResponse> notifications = notificationService.getNotifications(
                user.getUserId(), isRead, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping({"/unread-count", "/unread/count"})
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        User user = resolveAuthenticatedUser(authentication);
        Long count = notificationService.getUnreadCount(user.getUserId());
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable Long id,
            Authentication authentication) {
        User user = resolveAuthenticatedUser(authentication);
        NotificationResponse notification = notificationService.getNotificationById(id, user.getUserId());
        notificationService.markAsRead(id, user.getUserId());
        return ResponseEntity.ok(notification);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        User user = resolveAuthenticatedUser(authentication);
        NotificationResponse notification = notificationService.markAsRead(id, user.getUserId());
        return ResponseEntity.ok(notification);
    }

    @PatchMapping("/mark-read")
    public ResponseEntity<Void> markMultipleAsRead(
            @Valid @RequestBody MarkAsReadRequest request,
            Authentication authentication) {
        User user = resolveAuthenticatedUser(authentication);
        notificationService.markMultipleAsRead(request.getNotificationIds(), user.getUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {
        User user = resolveAuthenticatedUser(authentication);
        notificationService.deleteNotification(id, user.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> getPreferences(Authentication authentication) {
        User user = resolveAuthenticatedUser(authentication);
        NotificationPreferenceResponse preferences = preferenceService.getUserPreferences(user.getUserId());
        return ResponseEntity.ok(preferences);
    }

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            @Valid @RequestBody NotificationPreferenceRequest request,
            Authentication authentication) {
        User user = resolveAuthenticatedUser(authentication);
        NotificationPreferenceResponse preferences = preferenceService.updateUserPreferences(
                user.getUserId(), request);
        return ResponseEntity.ok(preferences);
    }

    private User resolveAuthenticatedUser(Authentication authentication) {
        Long userId = AuthenticationUtils.getUserId(authentication);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}
