package com.homeexpress.home_express_api.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.homeexpress.home_express_api.dto.request.AcceptQuotationRequest;
import com.homeexpress.home_express_api.dto.request.QuotationRequest;
import com.homeexpress.home_express_api.dto.response.AcceptQuotationResponse;
import com.homeexpress.home_express_api.dto.response.QuotationDetailResponse;
import com.homeexpress.home_express_api.dto.response.QuotationResponse;
import com.homeexpress.home_express_api.entity.QuotationStatus;
import com.homeexpress.home_express_api.service.QuotationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/quotations")
public class QuotationController {

    private final QuotationService quotationService;

    public QuotationController(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    @PostMapping
    public ResponseEntity<QuotationResponse> createQuotation(
            @Valid @RequestBody QuotationRequest request,
            @RequestParam Long transportId) {
        QuotationResponse response = quotationService.createQuotation(request, transportId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getQuotations(
    @RequestParam(required = false) Long bookingId,
    @RequestParam(required = false) Long transportId,
    @RequestParam(required = false) QuotationStatus status,
    Pageable pageable) {
    // Return detailed quotations when bookingId is provided (for frontend QuotationDetail)
    if (bookingId != null) {
            Page<QuotationDetailResponse> quotations = quotationService.getDetailedQuotations(bookingId, transportId, status, pageable);
            return ResponseEntity.ok(quotations);
        } else {
            Page<QuotationResponse> quotations = quotationService.getQuotations(bookingId, transportId, status, pageable);
            return ResponseEntity.ok(quotations);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuotationResponse> getQuotationById(@PathVariable Long id) {
        QuotationResponse response = quotationService.getQuotationById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<AcceptQuotationResponse> acceptQuotation(
            @PathVariable Long id,
            @RequestBody(required = false) AcceptQuotationRequest request,
            @RequestParam Long customerId,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        AcceptQuotationResponse response = quotationService.acceptQuotation(id, customerId, ipAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectQuotation(@PathVariable Long id) {
        quotationService.rejectQuotation(id);
        return ResponseEntity.ok(Map.of("message", "Quotation rejected successfully"));
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
}
