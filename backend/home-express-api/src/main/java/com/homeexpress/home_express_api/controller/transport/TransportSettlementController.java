package com.homeexpress.home_express_api.controller.transport;

import com.homeexpress.home_express_api.dto.SettlementDTO;
import com.homeexpress.home_express_api.dto.transport.TransportSettlementListDTO;
import com.homeexpress.home_express_api.dto.transport.TransportSettlementSummaryDTO;
import com.homeexpress.home_express_api.entity.BookingSettlement;
import com.homeexpress.home_express_api.entity.SettlementStatus;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.repository.BookingSettlementRepository;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.util.AuthenticationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transport/settlements")
public class TransportSettlementController {

    @Autowired
    private BookingSettlementRepository settlementRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getMySettlements(
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only transport companies can access this endpoint"));
        }

        Long transportId = user.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<BookingSettlement> settlements;
        if (status != null) {
            settlements = settlementRepository.findByTransportIdAndStatus(transportId, status);
        } else {
            settlements = settlementRepository.findByTransportId(transportId);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), settlements.size());
        List<BookingSettlement> paginatedList = settlements.subList(start, end);

        List<TransportSettlementListDTO> settlementDTOs = paginatedList.stream()
                .map(this::mapToListDTO)
                .collect(Collectors.toList());

        Page<TransportSettlementListDTO> settlementPage = new PageImpl<>(
                settlementDTOs,
                pageable,
                settlements.size()
        );

        return ResponseEntity.ok(Map.of(
                "settlements", settlementPage.getContent(),
                "currentPage", settlementPage.getNumber(),
                "totalItems", settlementPage.getTotalElements(),
                "totalPages", settlementPage.getTotalPages()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSettlementDetails(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only transport companies can access this endpoint"));
        }

        BookingSettlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Settlement not found"));

        if (!settlement.getTransportId().equals(user.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only view your own settlements"));
        }

        SettlementDTO dto = mapToFullDTO(settlement);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSettlementSummary(Authentication authentication) {

        User user = getUserFromAuth(authentication);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only transport companies can access this endpoint"));
        }

        Long transportId = user.getUserId();

        List<BookingSettlement> allSettlements = settlementRepository.findByTransportId(transportId);

        TransportSettlementSummaryDTO summary = new TransportSettlementSummaryDTO();

        List<BookingSettlement> pendingSettlements = allSettlements.stream()
                .filter(s -> s.getStatus() == SettlementStatus.PENDING)
                .collect(Collectors.toList());
        summary.setPendingCount(pendingSettlements.size());
        summary.setTotalPendingAmountVnd(pendingSettlements.stream()
                .mapToLong(BookingSettlement::getNetToTransportVnd)
                .sum());

        List<BookingSettlement> readySettlements = allSettlements.stream()
                .filter(s -> s.getStatus() == SettlementStatus.READY)
                .collect(Collectors.toList());
        summary.setReadyCount(readySettlements.size());
        summary.setTotalReadyAmountVnd(readySettlements.stream()
                .mapToLong(BookingSettlement::getNetToTransportVnd)
                .sum());

        List<BookingSettlement> inPayoutSettlements = allSettlements.stream()
                .filter(s -> s.getStatus() == SettlementStatus.IN_PAYOUT)
                .collect(Collectors.toList());
        summary.setInPayoutCount(inPayoutSettlements.size());
        summary.setTotalInPayoutAmountVnd(inPayoutSettlements.stream()
                .mapToLong(BookingSettlement::getNetToTransportVnd)
                .sum());

        List<BookingSettlement> paidSettlements = allSettlements.stream()
                .filter(s -> s.getStatus() == SettlementStatus.PAID)
                .collect(Collectors.toList());
        summary.setPaidCount(paidSettlements.size());
        summary.setTotalPaidAmountVnd(paidSettlements.stream()
                .mapToLong(BookingSettlement::getNetToTransportVnd)
                .sum());

        List<BookingSettlement> onHoldSettlements = allSettlements.stream()
                .filter(s -> s.getStatus() == SettlementStatus.ON_HOLD)
                .collect(Collectors.toList());
        summary.setOnHoldCount(onHoldSettlements.size());
        summary.setTotalOnHoldAmountVnd(onHoldSettlements.stream()
                .mapToLong(BookingSettlement::getNetToTransportVnd)
                .sum());

        return ResponseEntity.ok(summary);
    }

    private User getUserFromAuth(Authentication authentication) {
        return AuthenticationUtils.getUser(authentication, userRepository);
    }

    private TransportSettlementListDTO mapToListDTO(BookingSettlement settlement) {
        TransportSettlementListDTO dto = new TransportSettlementListDTO();
        dto.setSettlementId(settlement.getSettlementId());
        dto.setBookingId(settlement.getBookingId());
        dto.setAgreedPriceVnd(settlement.getAgreedPriceVnd());
        dto.setNetToTransportVnd(settlement.getNetToTransportVnd());
        dto.setCollectionMode(settlement.getCollectionMode());
        dto.setStatus(settlement.getStatus());
        dto.setCreatedAt(settlement.getCreatedAt());
        dto.setReadyAt(settlement.getReadyAt());
        dto.setPaidAt(settlement.getPaidAt());
        return dto;
    }

    private SettlementDTO mapToFullDTO(BookingSettlement settlement) {
        SettlementDTO dto = new SettlementDTO();
        dto.setSettlementId(settlement.getSettlementId());
        dto.setBookingId(settlement.getBookingId());
        dto.setTransportId(settlement.getTransportId());
        dto.setAgreedPriceVnd(settlement.getAgreedPriceVnd());
        dto.setTotalCollectedVnd(settlement.getTotalCollectedVnd());
        dto.setGatewayFeeVnd(settlement.getGatewayFeeVnd());
        dto.setCommissionRateBps(settlement.getCommissionRateBps());
        dto.setPlatformFeeVnd(settlement.getPlatformFeeVnd());
        dto.setAdjustmentVnd(settlement.getAdjustmentVnd());
        dto.setNetToTransportVnd(settlement.getNetToTransportVnd());
        dto.setCollectionMode(settlement.getCollectionMode());
        dto.setStatus(settlement.getStatus());
        dto.setOnHoldReason(settlement.getOnHoldReason());
        dto.setCreatedAt(settlement.getCreatedAt());
        dto.setReadyAt(settlement.getReadyAt());
        dto.setPaidAt(settlement.getPaidAt());
        dto.setUpdatedAt(settlement.getUpdatedAt());
        dto.setNotes(settlement.getNotes());
        return dto;
    }
}
