package com.homeexpress.home_express_api.controller.admin;

import com.homeexpress.home_express_api.dto.SettlementDTO;
import com.homeexpress.home_express_api.dto.admin.SettlementQueueResponse;
import com.homeexpress.home_express_api.dto.admin.SettlementReviewRequest;
import com.homeexpress.home_express_api.entity.SettlementStatus;
import com.homeexpress.home_express_api.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/settlements")
public class AdminSettlementController {

    @Autowired
    private SettlementService settlementService;

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Page<SettlementDTO>> getAllSettlements(
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(required = false) Long transportId,
            @PageableDefault(size = 20) Pageable pageable) {

        List<SettlementDTO> settlements;

        if (status != null && transportId != null) {
            settlements = settlementService.getSettlementsByTransport(transportId).stream()
                    .filter(s -> s.getStatus() == status)
                    .collect(Collectors.toList());
        } else if (status != null) {
            settlements = settlementService.getSettlementsByStatus(status);
        } else if (transportId != null) {
            settlements = settlementService.getSettlementsByTransport(transportId);
        } else {
            settlements = settlementService.getAllSettlements();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), settlements.size());
        
        List<SettlementDTO> pageContent = settlements.subList(start, end);
        Page<SettlementDTO> page = new PageImpl<>(pageContent, pageable, settlements.size());

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SettlementDTO> getSettlementById(@PathVariable Long id) {
        SettlementDTO settlement = settlementService.getSettlementById(id);
        return ResponseEntity.ok(settlement);
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> reviewSettlement(
            @PathVariable Long id,
            @Valid @RequestBody SettlementReviewRequest request) {

        SettlementDTO updatedSettlement = settlementService.updateSettlementStatus(
                id,
                request.getStatus(),
                request.getReason()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Settlement status updated successfully");
        response.put("settlement", updatedSettlement);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/queue")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SettlementQueueResponse> getSettlementQueue() {
        List<SettlementDTO> readySettlements = settlementService.getReadySettlementsGroupedByTransport();

        Map<Long, SettlementQueueResponse.TransportSettlementGroup> grouped = readySettlements.stream()
                .collect(Collectors.groupingBy(
                        SettlementDTO::getTransportId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    SettlementQueueResponse.TransportSettlementGroup group = 
                                        new SettlementQueueResponse.TransportSettlementGroup();
                                    group.setTransportId(list.get(0).getTransportId());
                                    group.setSettlements(list);
                                    group.setCount(list.size());
                                    group.setTotalAmount(
                                        list.stream()
                                            .mapToLong(SettlementDTO::getNetToTransportVnd)
                                            .sum()
                                    );
                                    return group;
                                }
                        )
                ));

        SettlementQueueResponse response = new SettlementQueueResponse();
        response.setSettlementsByTransport(grouped);
        response.setTotalTransports(grouped.size());
        response.setTotalSettlements(readySettlements.size());
        response.setTotalAmount(
            readySettlements.stream()
                .mapToLong(SettlementDTO::getNetToTransportVnd)
                .sum()
        );

        return ResponseEntity.ok(response);
    }
}
