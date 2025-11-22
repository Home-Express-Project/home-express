package com.homeexpress.home_express_api.controller.admin;

import com.homeexpress.home_express_api.dto.response.BookingTimelineResponse;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.service.BookingService;
import com.homeexpress.home_express_api.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin controller for booking management and oversight
 */
@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class AdminBookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    /**
     * Get comprehensive timeline for a booking
     * Includes status changes, quotations, and payments
     */
    @GetMapping("/{id}/timeline")
    public ResponseEntity<Map<String, Object>> getBookingTimeline(
            @PathVariable Long id,
            Authentication authentication) {
        
        User user = AuthenticationUtils.getUser(authentication, userRepository);
        if (user.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only managers can access booking timeline"));
        }

        try {
            BookingTimelineResponse timeline = bookingService.getBookingTimeline(id);
            Map<String, Object> response = new HashMap<>();
            response.put("bookingId", id);
            response.put("timeline", timeline.getTimeline());
            response.put("totalEvents", timeline.getTotalEvents());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to fetch timeline: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}

