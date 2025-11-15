package com.homeexpress.home_express_api.controller.customer;

import com.homeexpress.home_express_api.entity.Booking;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.repository.BookingRepository;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.service.CustomerEventService;
import com.homeexpress.home_express_api.util.AuthenticationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Server-Sent Events (SSE) endpoints for customer real-time updates
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customer")
public class CustomerSSEController {

    @Autowired
    private CustomerEventService customerEventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * SSE endpoint for real-time booking updates
     * Streams events for: status changes, new quotations, payment updates, transport assignment
     * 
     * @param bookingId The booking ID to watch
     * @param authentication Spring Security authentication
     * @return SseEmitter for the connection
     */
    @GetMapping(value = "/bookings/{bookingId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<?> streamBookingEvents(
            @PathVariable Long bookingId,
            Authentication authentication) {

        try {
            // Authenticate user
            User user = AuthenticationUtils.getUser(authentication, userRepository);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            // Verify user is a customer
            if (user.getRole() != UserRole.CUSTOMER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Only customers can access booking events"));
            }

            // Verify booking exists
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Booking not found"));
            }

            // Verify customer owns the booking
            if (!booking.getCustomerId().equals(user.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You can only watch your own bookings"));
            }

            // Create SSE connection
            SseEmitter emitter = customerEventService.createEventStream(bookingId, user.getUserId());
            
            log.info("Customer {} connected to SSE stream for booking {}", user.getUserId(), bookingId);
            
            return ResponseEntity.ok()
                    .header("Cache-Control", "no-cache")
                    .header("X-Accel-Buffering", "no") // Disable nginx buffering
                    .body(emitter);

        } catch (Exception e) {
            log.error("Error creating SSE stream for booking {}: {}", bookingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to establish connection: " + e.getMessage()));
        }
    }

    /**
     * Get SSE connection statistics (for debugging/monitoring)
     */
    @GetMapping("/events/stats")
    public ResponseEntity<Map<String, Object>> getConnectionStats(Authentication authentication) {
        try {
            User user = AuthenticationUtils.getUser(authentication, userRepository);
            
            if (user == null || user.getRole() != UserRole.CUSTOMER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalActiveConnections", customerEventService.getTotalActiveConnections());
            stats.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error getting SSE stats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Scheduled task to send heartbeat to all active connections
     * Runs every 30 seconds to keep connections alive
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void sendHeartbeats() {
        // This would need to iterate through all active bookings
        // For now, we'll let the service handle individual heartbeats
        // when events occur. A full implementation would track all active
        // booking IDs and send heartbeats to each.
        
        int activeConnections = customerEventService.getTotalActiveConnections();
        if (activeConnections > 0) {
            log.debug("Active SSE connections: {}", activeConnections);
        }
    }

    /**
     * Create error response map
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}

