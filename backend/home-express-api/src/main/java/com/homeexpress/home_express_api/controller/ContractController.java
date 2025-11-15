package com.homeexpress.home_express_api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.homeexpress.home_express_api.dto.request.ContractRequest;
import com.homeexpress.home_express_api.dto.request.ContractUpdateRequest;
import com.homeexpress.home_express_api.dto.response.ContractResponse;
import com.homeexpress.home_express_api.entity.ContractStatus;
import com.homeexpress.home_express_api.entity.Transport;
import com.homeexpress.home_express_api.service.ContractService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/transport/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    public ResponseEntity<ContractResponse> createContract(
            @Valid @RequestBody ContractRequest request,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String userRole) {
        ContractResponse response = contractService.createContract(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ContractResponse>> getContracts(
            @RequestParam(required = false) ContractStatus status,
            Pageable pageable,
            @AuthenticationPrincipal Transport transport) {

        if (transport == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long transportId = transport.getTransportId();

        if (status != null) {
            return ResponseEntity.ok(contractService.getContractsByTransportIdAndStatus(transportId, status, pageable));
        }

        return ResponseEntity.ok(contractService.getContractsByTransportId(transportId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractResponse> getContractById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String userRole) {
        ContractResponse response = contractService.getContractById(id);

        if ("CUSTOMER".equalsIgnoreCase(userRole)) {
            Long contractCustomerId = contractService.getCustomerIdByContractId(id);
            if (!contractCustomerId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else if ("TRANSPORT".equalsIgnoreCase(userRole)) {
            Long contractTransportId = contractService.getTransportIdByContractId(id);
            if (!contractTransportId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{contractNumber}")
    public ResponseEntity<ContractResponse> getContractByNumber(@PathVariable String contractNumber) {
        ContractResponse response = contractService.getContractByNumber(contractNumber);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ContractResponse> updateContractStatus(
            @PathVariable Long id,
            @Valid @RequestBody ContractUpdateRequest request) {
        ContractResponse response = contractService.updateContractStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/sign")
    public ResponseEntity<ContractResponse> signContract(
            @PathVariable Long id,
            @RequestParam String role,
            @RequestParam(required = false) String signatureUrl,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        ContractResponse response = contractService.signContract(id, role, signatureUrl, ipAddress);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
