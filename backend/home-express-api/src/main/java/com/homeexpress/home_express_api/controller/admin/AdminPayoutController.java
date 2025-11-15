package com.homeexpress.home_express_api.controller.admin;

import com.homeexpress.home_express_api.dto.admin.CreatePayoutRequest;
import com.homeexpress.home_express_api.dto.admin.UpdatePayoutStatusRequest;
import com.homeexpress.home_express_api.dto.payout.PayoutDTO;
import com.homeexpress.home_express_api.entity.PayoutStatus;
import com.homeexpress.home_express_api.service.PayoutService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/payouts")
public class AdminPayoutController {

    @Autowired
    private PayoutService payoutService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> createPayout(
            @Valid @RequestBody CreatePayoutRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (Boolean.TRUE.equals(request.getAllTransports())) {
                List<PayoutDTO> payouts = payoutService.createPayoutBatchForAllTransports();
                response.put("success", true);
                response.put("message", "Payouts created for " + payouts.size() + " transports");
                response.put("payouts", payouts);
                response.put("count", payouts.size());
            } else {
                if (request.getTransportId() == null) {
                    response.put("success", false);
                    response.put("message", "Transport ID is required when allTransports is false");
                    return ResponseEntity.badRequest().body(response);
                }
                PayoutDTO payout = payoutService.createPayoutBatch(request.getTransportId());
                response.put("success", true);
                response.put("message", "Payout created successfully");
                response.put("payout", payout);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create payout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Page<PayoutDTO>> getAllPayouts(
            @RequestParam(required = false) PayoutStatus status,
            @RequestParam(required = false) Long transportId,
            @PageableDefault(size = 20) Pageable pageable) {

        List<PayoutDTO> payouts;

        if (status != null && transportId != null) {
            payouts = payoutService.getPayoutsByTransport(transportId).stream()
                    .filter(p -> p.getStatus() == status)
                    .collect(Collectors.toList());
        } else if (status != null) {
            payouts = payoutService.getPayoutsByStatus(status);
        } else if (transportId != null) {
            payouts = payoutService.getPayoutsByTransport(transportId);
        } else {
            payouts = payoutService.getAllPayouts();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), payouts.size());

        List<PayoutDTO> pageContent = payouts.subList(start, end);
        Page<PayoutDTO> page = new PageImpl<>(pageContent, pageable, payouts.size());

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PayoutDTO> getPayoutById(@PathVariable Long id) {
        PayoutDTO payout = payoutService.getPayoutDetails(id);
        return ResponseEntity.ok(payout);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> updatePayoutStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePayoutStatusRequest request) {

        PayoutDTO updatedPayout = payoutService.updatePayoutStatus(
                id,
                request.getStatus(),
                request.getFailureReason(),
                request.getTransactionReference()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Payout status updated to " + request.getStatus());
        response.put("payout", updatedPayout);

        return ResponseEntity.ok(response);
    }
}
