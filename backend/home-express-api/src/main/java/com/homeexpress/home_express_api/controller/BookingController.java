package com.homeexpress.home_express_api.controller;

import com.homeexpress.home_express_api.dto.booking.*;
import com.homeexpress.home_express_api.dto.request.ConfirmCompletionRequest;
import com.homeexpress.home_express_api.dto.request.UploadBookingEvidenceRequest;
import com.homeexpress.home_express_api.dto.response.BookingEvidenceResponse;
import com.homeexpress.home_express_api.dto.response.QuotationResponse;
import com.homeexpress.home_express_api.entity.EvidenceType;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.service.BookingService;
import com.homeexpress.home_express_api.service.EvidenceService;
import com.homeexpress.home_express_api.util.AuthenticationUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EvidenceService evidenceService;

    @PostMapping
    public ResponseEntity<?> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        
        User user = AuthenticationUtils.getUser(authentication, userRepository);

        if (user.getRole() != UserRole.CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Only customers can create bookings"));
        }

        BookingResponse response = bookingService.createBooking(request, user.getUserId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "Booking created successfully",
            "booking", response
        ));
    }

    @GetMapping
    public ResponseEntity<?> getBookings(
            @RequestParam(required = false) Long customerId,
            Authentication authentication) {
        
        User user = AuthenticationUtils.getUser(authentication, userRepository);

        List<BookingResponse> bookings;

        if (user.getRole() == UserRole.MANAGER) {
            if (customerId != null) {
                bookings = bookingService.getBookingsByCustomer(customerId, user.getUserId(), user.getRole());
            } else {
                bookings = bookingService.getAllBookings(user.getRole());
            }
        } else if (user.getRole() == UserRole.CUSTOMER) {
            bookings = bookingService.getBookingsByCustomer(user.getUserId(), user.getUserId(), user.getRole());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied"));
        }

        return ResponseEntity.ok(Map.of(
            "bookings", bookings,
            "count", bookings.size()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(
            @PathVariable Long id,
            Authentication authentication) {
        
        User user = AuthenticationUtils.getUser(authentication, userRepository);

        BookingResponse booking = bookingService.getBookingById(id, user.getUserId(), user.getRole());
        
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingUpdateRequest request,
            Authentication authentication) {
        
        User user = AuthenticationUtils.getUser(authentication, userRepository);

        BookingResponse response = bookingService.updateBooking(id, request, user.getUserId(), user.getRole());
        
        return ResponseEntity.ok(Map.of(
            "message", "Booking updated successfully",
            "booking", response
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        
        User user = AuthenticationUtils.getUser(authentication, userRepository);

        String reason = body != null ? body.get("reason") : null;
        
        bookingService.cancelBooking(id, reason, user.getUserId(), user.getRole());
        
        return ResponseEntity.ok(Map.of(
            "message", "Booking cancelled successfully",
            "bookingId", id
        ));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<?> getBookingHistory(
    @PathVariable Long id,
    Authentication authentication) {

    User user = AuthenticationUtils.getUser(authentication, userRepository);

    List<BookingStatusHistoryResponse> history =
    bookingService.getBookingHistory(id, user.getUserId(), user.getRole());

    return ResponseEntity.ok(Map.of(
    "bookingId", id,
    "history", history,
    "count", history.size()
    ));
    }

    @GetMapping("/{id}/quotations")
    public ResponseEntity<?> getBookingQuotations(
            @PathVariable Long id,
            Authentication authentication) {

        User user = AuthenticationUtils.getUser(authentication, userRepository);

        // Check if user has access to this booking
        BookingResponse booking = bookingService.getBookingById(id, user.getUserId(), user.getRole());

        List<QuotationResponse> quotations = bookingService.getBookingQuotations(id, user.getUserId(), user.getRole());

        return ResponseEntity.ok(Map.of(
            "bookingId", id,
            "quotations", quotations,
            "count", quotations.size()
        ));
    }

    /**
     * Customer confirms booking completion after service is done
     * This triggers status change to CONFIRMED_BY_CUSTOMER and settlement becomes READY
     */
    @PostMapping("/{id}/confirm-completion")
    public ResponseEntity<?> confirmCompletion(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ConfirmCompletionRequest request,
            Authentication authentication) {

        User user = AuthenticationUtils.getUser(authentication, userRepository);

        if (user.getRole() != UserRole.CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Only customers can confirm booking completion"));
        }

        BookingResponse response = bookingService.confirmBookingCompletion(id, request, user.getUserId());

        return ResponseEntity.ok(Map.of(
            "message", "Booking completion confirmed successfully",
            "booking", response
        ));
    }

    // ========================================================================
    // EVIDENCE ENDPOINTS
    // ========================================================================

    /**
     * Get all evidence for a booking
     * Supports filtering by evidence type
     */
    @GetMapping("/{bookingId}/evidence")
    public ResponseEntity<?> getBookingEvidence(
            @PathVariable Long bookingId,
            @RequestParam(required = false) EvidenceType type,
            Authentication authentication) {

        User user = AuthenticationUtils.getUser(authentication, userRepository);

        List<BookingEvidenceResponse> evidence = evidenceService.getBookingEvidence(
                bookingId,
                user.getUserId(),
                user.getRole(),
                type
        );

        return ResponseEntity.ok(Map.of(
                "bookingId", bookingId,
                "evidence", evidence,
                "count", evidence.size()
        ));
    }

    /**
     * Upload evidence for a booking
     * Customers and transport providers can upload evidence
     */
    @PostMapping("/{bookingId}/evidence")
    public ResponseEntity<?> uploadBookingEvidence(
            @PathVariable Long bookingId,
            @Valid @RequestBody UploadBookingEvidenceRequest request,
            Authentication authentication) {

        User user = AuthenticationUtils.getUser(authentication, userRepository);

        // Only customers and transport can upload evidence
        if (user.getRole() != UserRole.CUSTOMER && user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only customers and transport providers can upload evidence"));
        }

        BookingEvidenceResponse response = evidenceService.uploadBookingEvidence(
                bookingId,
                request,
                user.getUserId(),
                user.getRole()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Evidence uploaded successfully",
                "evidence", response
        ));
    }

    /**
     * Delete evidence
     * Only the uploader or managers can delete evidence
     */
    @DeleteMapping("/evidence/{evidenceId}")
    public ResponseEntity<?> deleteEvidence(
            @PathVariable Long evidenceId,
            Authentication authentication) {

        User user = AuthenticationUtils.getUser(authentication, userRepository);

        evidenceService.deleteEvidence(evidenceId, user.getUserId(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "message", "Evidence deleted successfully"
        ));
    }

}
