package com.homeexpress.home_express_api.controller;

import com.homeexpress.home_express_api.dto.request.AddDisputeMessageRequest;
import com.homeexpress.home_express_api.dto.request.CreateDisputeRequest;
import com.homeexpress.home_express_api.dto.response.DisputeMessageResponse;
import com.homeexpress.home_express_api.dto.response.DisputeResponse;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for customer dispute operations.
 * Handles dispute creation, viewing, messaging, and evidence attachment.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerDisputeController {

    private final DisputeService disputeService;

    /**
     * File a new dispute for a booking
     * POST /api/v1/customer/bookings/{bookingId}/disputes
     */
    @PostMapping("/bookings/{bookingId}/disputes")
    public ResponseEntity<DisputeResponse> createDispute(
            @PathVariable Long bookingId,
            @Valid @RequestBody CreateDisputeRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Customer {} filing dispute for booking {}", user.getUserId(), bookingId);
        
        DisputeResponse dispute = disputeService.createDispute(
                bookingId, request, user.getUserId(), user.getRole());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dispute);
    }

    /**
     * Get all disputes for a booking
     * GET /api/v1/customer/bookings/{bookingId}/disputes
     */
    @GetMapping("/bookings/{bookingId}/disputes")
    public ResponseEntity<Map<String, Object>> getBookingDisputes(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal User user) {
        
        log.debug("Customer {} getting disputes for booking {}", user.getUserId(), bookingId);
        
        List<DisputeResponse> disputes = disputeService.getBookingDisputes(
                bookingId, user.getUserId(), user.getRole());
        
        return ResponseEntity.ok(Map.of(
                "disputes", disputes,
                "count", disputes.size()
        ));
    }

    /**
     * Get a single dispute by ID
     * GET /api/v1/customer/disputes/{disputeId}
     */
    @GetMapping("/disputes/{disputeId}")
    public ResponseEntity<DisputeResponse> getDispute(
            @PathVariable Long disputeId,
            @AuthenticationPrincipal User user) {
        
        log.debug("Customer {} getting dispute {}", user.getUserId(), disputeId);
        
        DisputeResponse dispute = disputeService.getDisputeById(
                disputeId, user.getUserId(), user.getRole());
        
        return ResponseEntity.ok(dispute);
    }

    /**
     * Add a message to a dispute thread
     * POST /api/v1/customer/disputes/{disputeId}/messages
     */
    @PostMapping("/disputes/{disputeId}/messages")
    public ResponseEntity<DisputeMessageResponse> addMessage(
            @PathVariable Long disputeId,
            @Valid @RequestBody AddDisputeMessageRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Customer {} adding message to dispute {}", user.getUserId(), disputeId);
        
        DisputeMessageResponse message = disputeService.addDisputeMessage(
                disputeId, request, user.getUserId(), user.getRole());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    /**
     * Get all messages for a dispute
     * GET /api/v1/customer/disputes/{disputeId}/messages
     */
    @GetMapping("/disputes/{disputeId}/messages")
    public ResponseEntity<Map<String, Object>> getDisputeMessages(
            @PathVariable Long disputeId,
            @AuthenticationPrincipal User user) {
        
        log.debug("Customer {} getting messages for dispute {}", user.getUserId(), disputeId);
        
        List<DisputeMessageResponse> messages = disputeService.getDisputeMessages(
                disputeId, user.getUserId(), user.getRole());
        
        return ResponseEntity.ok(Map.of(
                "messages", messages,
                "count", messages.size()
        ));
    }

    /**
     * Attach evidence to a dispute
     * POST /api/v1/customer/disputes/{disputeId}/evidence/{evidenceId}
     */
    @PostMapping("/disputes/{disputeId}/evidence/{evidenceId}")
    public ResponseEntity<Map<String, String>> attachEvidence(
            @PathVariable Long disputeId,
            @PathVariable Long evidenceId,
            @AuthenticationPrincipal User user) {
        
        log.info("Customer {} attaching evidence {} to dispute {}", 
                user.getUserId(), evidenceId, disputeId);
        
        disputeService.attachEvidence(disputeId, evidenceId, user.getUserId(), user.getRole());
        
        return ResponseEntity.ok(Map.of(
                "message", "Evidence attached successfully",
                "disputeId", disputeId.toString(),
                "evidenceId", evidenceId.toString()
        ));
    }
}

