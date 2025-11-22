package com.homeexpress.home_express_api.controller.customer;

import com.homeexpress.home_express_api.dto.booking.BookingResponse;
import com.homeexpress.home_express_api.dto.response.CustomerDashboardStatsResponse;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.service.BookingService;
import com.homeexpress.home_express_api.service.CustomerDashboardService;
import com.homeexpress.home_express_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.homeexpress.home_express_api.util.AuthenticationUtils;

@RestController
@RequestMapping("/api/v1/customer")
public class CustomerDashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerDashboardService customerDashboardService;

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getDashboardStats(Authentication authentication) {
        User customer = resolveAuthenticatedCustomer(authentication);
        CustomerDashboardStatsResponse stats = customerDashboardService.getDashboardStats(customer.getUserId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getCustomerBookings(Authentication authentication) {
        User customer = resolveAuthenticatedCustomer(authentication);
        List<BookingResponse> bookings = bookingService.getBookingsByCustomer(
                customer.getUserId(),
                customer.getUserId(),
                customer.getRole()
        );

        List<Map<String, Object>> payload = bookings.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());

        return ResponseEntity.ok(payload);
    }

    private Map<String, Object> toSummary(BookingResponse booking) {
        return Map.ofEntries(
                Map.entry("booking_id", booking.getBookingId()),
                Map.entry("customer_id", booking.getCustomerId()),
                Map.entry("transport_id", booking.getTransportId()),
                Map.entry("status", booking.getStatus() != null ? booking.getStatus().name() : null),
                Map.entry("pickup_address", booking.getPickupAddress() != null ? booking.getPickupAddress().getAddressLine() : null),
                Map.entry("delivery_address", booking.getDeliveryAddress() != null ? booking.getDeliveryAddress().getAddressLine() : null),
                Map.entry("pickup_date", booking.getPreferredDate() != null ? booking.getPreferredDate().toString() : null),
                Map.entry("estimated_price", booking.getEstimatedPrice()),
                Map.entry("final_price", booking.getFinalPrice()),
                Map.entry("created_at", booking.getCreatedAt() != null ? booking.getCreatedAt().atOffset(ZoneOffset.UTC).toString() : null),
                Map.entry("updated_at", booking.getUpdatedAt() != null ? booking.getUpdatedAt().atOffset(ZoneOffset.UTC).toString() : null)
        );
    }

    private User resolveAuthenticatedCustomer(Authentication authentication) {
        Long userId = AuthenticationUtils.getUserId(authentication);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customers can access this resource");
        }

        return user;
    }
}
