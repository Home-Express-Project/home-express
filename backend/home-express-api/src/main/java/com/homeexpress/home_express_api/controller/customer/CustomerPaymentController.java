package com.homeexpress.home_express_api.controller.customer;

import com.homeexpress.home_express_api.dto.payment.*;
import com.homeexpress.home_express_api.dto.request.InitiateDepositRequest;
import com.homeexpress.home_express_api.dto.request.InitiateRemainingPaymentRequest;
import com.homeexpress.home_express_api.dto.response.BankInfoResponse;
import com.homeexpress.home_express_api.dto.response.InitiateDepositResponse;
import com.homeexpress.home_express_api.dto.response.InitiateRemainingPaymentResponse;
import com.homeexpress.home_express_api.dto.response.PaymentStatusResponse;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.service.PaymentService;
import com.homeexpress.home_express_api.util.AuthenticationUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CustomerPaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/bookings/{bookingId}/payments/summary")
    public ResponseEntity<?> getPaymentSummary(
            @PathVariable Long bookingId,
            Authentication authentication) {

        User user = AuthenticationUtils.getUser(authentication, userRepository);

        PaymentSummaryDTO summary = paymentService.getPaymentSummary(bookingId, user.getUserId(), user.getRole());

        return ResponseEntity.ok(summary);
    }

    @PostMapping("/payments/init")
    public ResponseEntity<?> initializePayment(
            @Valid @RequestBody PaymentInitRequestDTO request,
            Authentication authentication) {

        User user = AuthenticationUtils.getUser(authentication, userRepository);

        if (user.getRole() != UserRole.CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only customers can initialize payments"));
        }

        PaymentResponseDTO response = paymentService.initializePayment(request, user.getUserId(), user.getRole());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Payment initialized successfully",
                "payment", response
        ));
    }

    @PostMapping("/payments/confirm")
    public ResponseEntity<?> confirmPayment(
            @Valid @RequestBody PaymentConfirmRequestDTO request,
            Authentication authentication) {

        User user = AuthenticationUtils.getUser(authentication, userRepository);

        PaymentResponseDTO response = paymentService.confirmPayment(request, user.getUserId(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "message", "Payment confirmed successfully",
                "payment", response
        ));
    }

    @GetMapping("/bookings/{bookingId}/payments")
    public ResponseEntity<?> getPaymentHistory(
            @PathVariable Long bookingId,
            Authentication authentication) {

        User user = AuthenticationUtils.getUser(authentication, userRepository);

        List<PaymentResponseDTO> payments = paymentService.getPaymentHistory(bookingId, user.getUserId(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "bookingId", bookingId,
                "payments", payments,
                "count", payments.size()
        ));
    }
    
    // ========================================================================
    // NEW ENDPOINTS FOR CUSTOMER BOOKING FLOW - PRODUCTION READY
    // ========================================================================
    
    /**
     * Initiate deposit payment (30% of booking total).
     * Supports CASH and BANK_TRANSFER (no external payment gateways).
     */
    @PostMapping("/customer/payments/deposit/initiate")
    public ResponseEntity<InitiateDepositResponse> initiateDeposit(
            @Valid @RequestBody InitiateDepositRequest request,
            Authentication authentication) {
        
        try {
            User user = AuthenticationUtils.getUser(authentication, userRepository);
            
            if (user.getRole() != UserRole.CUSTOMER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(InitiateDepositResponse.builder()
                                .success(false)
                                .message("Only customers can initiate payments")
                                .build());
            }
            
            InitiateDepositResponse response = paymentService.initiateDepositPayment(request, user.getUserId());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(InitiateDepositResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Initiate remaining payment (70% of booking total + optional tip).
     * Supports CASH and BANK_TRANSFER (no external payment gateways).
     */
    @PostMapping("/customer/payments/remaining/initiate")
    public ResponseEntity<InitiateRemainingPaymentResponse> initiateRemainingPayment(
            @Valid @RequestBody InitiateRemainingPaymentRequest request,
            Authentication authentication) {

        try {
            User user = AuthenticationUtils.getUser(authentication, userRepository);

            if (user.getRole() != UserRole.CUSTOMER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(InitiateRemainingPaymentResponse.builder()
                                .success(false)
                                .message("Only customers can initiate payments")
                                .build());
            }

            InitiateRemainingPaymentResponse response = paymentService.initiateRemainingPayment(request, user.getUserId());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(InitiateRemainingPaymentResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Get payment status for a booking
     * Used by frontend to poll payment status (bank transfer flow)
     */
    @GetMapping("/customer/payments/status")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @RequestParam Long bookingId,
            @RequestParam(required = false) String paymentId,
            Authentication authentication) {
        
        try {
            User user = AuthenticationUtils.getUser(authentication, userRepository);
            
            PaymentStatusResponse response = paymentService.getPaymentStatus(bookingId, paymentId, user.getUserId());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(PaymentStatusResponse.builder()
                            .success(false)
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());
        }
    }
    
    /**
     * Get bank transfer information.
     * Returns configured bank account details (no QR code).
     */
    @GetMapping("/customer/payments/bank-info")
    public ResponseEntity<BankInfoResponse> getBankInfo(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Double amount) {
        
        try {
            BankInfoResponse response;
            
            if (bookingId != null && amount != null) {
                // Return bank info for a specific booking (amount can be used for display if needed)
                response = paymentService.getBankInfo(bookingId, amount);
            } else {
                // Return generic bank info
                response = paymentService.getBankInfoGeneric();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(BankInfoResponse.builder()
                            .bank("Error")
                            .accountNumber("")
                            .accountName(e.getMessage())
                            .build());
        }
    }
}
