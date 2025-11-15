package com.homeexpress.home_express_api.controller.admin;

import com.homeexpress.home_express_api.dto.response.UserListResponse;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.entity.UserSession;
import com.homeexpress.home_express_api.service.AdminUserService;
import com.homeexpress.home_express_api.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUsersController {

    @Autowired
    private AdminUserService adminUserService;
    
    @Autowired
    private UserSessionService userSessionService;

    /**
     * Get all users with profiles and pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserListResponse> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        UserListResponse response = adminUserService.getAllUsersWithProfiles(
            role, status, search, page - 1, limit); // Convert to 0-based
        return ResponseEntity.ok(response);
    }

    /**
     * Get user by ID with profile
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        Map<String, Object> response = adminUserService.getUserWithProfile(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate user
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> activateUser(@PathVariable Long id) {
        adminUserService.activateUser(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User activated successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate user
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> deactivateUser(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload) {
        
        String reason = payload != null ? payload.get("reason") : "Deactivated by admin";
        adminUserService.deactivateUser(id, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User deactivated successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user has active bookings
     */
    @GetMapping("/{id}/active-bookings")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> checkActiveBookings(@PathVariable Long id) {
        Map<String, Object> result = adminUserService.checkUserActiveBookings(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", result);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all active sessions for a user
     */
    @GetMapping("/{id}/sessions")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getUserSessions(@PathVariable Long id) {
        List<UserSession> sessions = userSessionService.getActiveSessions(id);
        
        List<Map<String, Object>> sessionData = sessions.stream().map(session -> {
            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", session.getSessionId());
            data.put("ipAddress", session.getIpAddress());
            data.put("userAgent", session.getUserAgent());
            data.put("deviceId", session.getDeviceId());
            data.put("createdAt", session.getCreatedAt());
            data.put("lastSeenAt", session.getLastSeenAt());
            data.put("expiresAt", session.getExpiresAt());
            data.put("revokedAt", session.getRevokedAt());
            data.put("revokedReason", session.getRevokedReason());
            return data;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", id);
        response.put("sessionCount", sessions.size());
        response.put("sessions", sessionData);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Revoke a specific session
     */
    @DeleteMapping("/{id}/sessions/{sessionId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> revokeSession(
            @PathVariable Long id,
            @PathVariable String sessionId,
            @RequestBody(required = false) Map<String, String> payload) {
        
        String reason = payload != null && payload.containsKey("reason") 
            ? payload.get("reason") 
            : "Revoked by admin";
        
        userSessionService.revokeSession(sessionId, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Session revoked successfully");
        response.put("sessionId", sessionId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Revoke all sessions for a user
     */
    @DeleteMapping("/{id}/sessions")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> revokeAllUserSessions(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload) {
        
        String reason = payload != null && payload.containsKey("reason") 
            ? payload.get("reason") 
            : "All sessions revoked by admin";
        
        int revokedCount = userSessionService.revokeAllUserSessions(id, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All sessions revoked successfully");
        response.put("revokedCount", revokedCount);
        
        return ResponseEntity.ok(response);
    }
}
