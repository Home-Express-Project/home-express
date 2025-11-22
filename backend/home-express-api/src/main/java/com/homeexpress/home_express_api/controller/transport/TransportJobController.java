package com.homeexpress.home_express_api.controller.transport;

import com.homeexpress.home_express_api.dto.response.ApiResponse;
import com.homeexpress.home_express_api.dto.response.ReadyToQuoteStatusResponse;
import com.homeexpress.home_express_api.dto.response.SuggestedPriceResponse;
import com.homeexpress.home_express_api.dto.transport.TransportActiveJobDetailDto;
import com.homeexpress.home_express_api.dto.transport.TransportActiveJobSummaryDto;
import com.homeexpress.home_express_api.dto.transport.TransportAvailableBookingDto;
import com.homeexpress.home_express_api.dto.transport.TransportPaginatedResponse;
import com.homeexpress.home_express_api.entity.Transport;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.repository.TransportRepository;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.service.PricingService;
import com.homeexpress.home_express_api.service.RateCardService;
import com.homeexpress.home_express_api.service.TransportJobService;
import com.homeexpress.home_express_api.util.AuthenticationUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/transport")
@Validated
public class TransportJobController {

    private final TransportJobService transportJobService;
    private final UserRepository userRepository;
    private final TransportRepository transportRepository;
    private final RateCardService rateCardService;
    private final PricingService pricingService;

    public TransportJobController(TransportJobService transportJobService,
                                  UserRepository userRepository,
                                  TransportRepository transportRepository,
                                  RateCardService rateCardService,
                                  PricingService pricingService) {
        this.transportJobService = transportJobService;
        this.userRepository = userRepository;
        this.transportRepository = transportRepository;
        this.rateCardService = rateCardService;
        this.pricingService = pricingService;
    }

    @GetMapping("/bookings/available")
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<?> getAvailableBookings(
            Authentication authentication,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
            @RequestParam(required = false) Integer maxDistance,
            @RequestParam(required = false) String preferredDate
    ) {
        User user = AuthenticationUtils.getUser(authentication, userRepository);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(403).body(ApiResponse.error("Only transport accounts can access this resource"));
        }

        Optional<Transport> transportOpt = transportRepository.findByUser_UserId(user.getUserId());
        if (transportOpt.isEmpty()) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Transport profile not found. Please complete transport registration."));
        }

        Transport transport = transportOpt.get();
        if (!Boolean.TRUE.equals(transport.getReadyToQuote())) {
            return buildReadyToQuoteForbiddenResponse(transport.getTransportId());
        }

        LocalDate dateFilter = null;
        if (preferredDate != null && !preferredDate.isBlank()) {
            try {
                dateFilter = LocalDate.parse(preferredDate);
            } catch (DateTimeParseException ignored) {
            }
        }

        TransportPaginatedResponse<TransportAvailableBookingDto> response =
                transportJobService.getAvailableBookings(user.getUserId(), page, limit, maxDistance, dateFilter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookings/{bookingId}/suggested-price")
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<?> getSuggestedPrice(
            Authentication authentication,
            @PathVariable Long bookingId
    ) {
        User user = AuthenticationUtils.getUser(authentication, userRepository);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(403).body(ApiResponse.error("Only transport accounts can access this resource"));
        }

        Optional<Transport> transportOpt = transportRepository.findByUser_UserId(user.getUserId());
        if (transportOpt.isEmpty()) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Transport profile not found. Please complete transport registration."));
        }

        Transport transport = transportOpt.get();
        if (!Boolean.TRUE.equals(transport.getReadyToQuote())) {
            return buildReadyToQuoteForbiddenResponse(transport.getTransportId());
        }

        SuggestedPriceResponse response = pricingService.calculateSuggestedPrice(bookingId, transport.getTransportId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @GetMapping("/active-jobs")
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<?> getActiveJobs(Authentication authentication) {
        User user = AuthenticationUtils.getUser(authentication, userRepository);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(403).body(ApiResponse.error("Only transport accounts can access this resource"));
        }

        List<TransportActiveJobSummaryDto> jobs = transportJobService.getActiveJobs(user.getUserId());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/bookings/available/{bookingId}")
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<?> getAvailableBookingDetail(Authentication authentication, @PathVariable Long bookingId) {
        User user = AuthenticationUtils.getUser(authentication, userRepository);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(403).body(ApiResponse.error("Only transport accounts can access this resource"));
        }

        Optional<Transport> transportOpt = transportRepository.findByUser_UserId(user.getUserId());
        if (transportOpt.isEmpty()) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Transport profile not found. Please complete transport registration."));
        }

        Transport transport = transportOpt.get();
        if (!Boolean.TRUE.equals(transport.getReadyToQuote())) {
            return buildReadyToQuoteForbiddenResponse(transport.getTransportId());
        }

        Optional<TransportActiveJobDetailDto> booking = transportJobService.getAvailableBookingDetail(bookingId);
        return booking.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Booking not found or not available")));
    }

    @GetMapping("/active-jobs/{bookingId}")
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<?> getActiveJobDetail(Authentication authentication, @PathVariable Long bookingId) {
        User user = AuthenticationUtils.getUser(authentication, userRepository);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(403).body(ApiResponse.error("Only transport accounts can access this resource"));
        }

        Optional<TransportActiveJobDetailDto> job = transportJobService.getActiveJobDetail(bookingId, user.getUserId());
        return job.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Job not found")));
    }

    @PutMapping("/bookings/{bookingId}/start")
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<?> startJob(Authentication authentication, @PathVariable Long bookingId) {
        User user = AuthenticationUtils.getUser(authentication, userRepository);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(403).body(ApiResponse.error("Only transport accounts can access this resource"));
        }

        Optional<Transport> transportOpt = transportRepository.findByUser_UserId(user.getUserId());
        if (transportOpt.isEmpty()) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Transport profile not found. Please complete transport registration."));
        }

        try {
            com.homeexpress.home_express_api.entity.Booking booking = transportJobService.startJob(bookingId, transportOpt.get().getTransportId());
            Map<String, Object> response = Map.of(
                    "message", "Job started successfully",
                    "booking", Map.of(
                            "bookingId", booking.getBookingId(),
                            "status", booking.getStatus().name(),
                            "scheduledDatetime", booking.getPreferredDate().toString()
                    )
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/bookings/{bookingId}/complete")
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<?> completeJob(
            Authentication authentication,
            @PathVariable Long bookingId,
            @RequestBody(required = false) Map<String, Object> requestBody
    ) {
        User user = AuthenticationUtils.getUser(authentication, userRepository);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(403).body(ApiResponse.error("Only transport accounts can access this resource"));
        }

        Optional<Transport> transportOpt = transportRepository.findByUser_UserId(user.getUserId());
        if (transportOpt.isEmpty()) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Transport profile not found. Please complete transport registration."));
        }

        String completionNotes = null;
        List<String> completionPhotos = null;
        
        if (requestBody != null) {
            completionNotes = (String) requestBody.get("completionNotes");
            Object photosObj = requestBody.get("completionPhotos");
            if (photosObj instanceof List<?>) {
                completionPhotos = ((List<?>) photosObj).stream()
                        .filter(o -> o instanceof String)
                        .map(o -> (String) o)
                        .collect(java.util.stream.Collectors.toList());
            }
        }

        try {
            com.homeexpress.home_express_api.entity.Booking booking = transportJobService.completeJob(
                    bookingId,
                    transportOpt.get().getTransportId(),
                    completionNotes,
                    completionPhotos
            );
            Map<String, Object> response = Map.of(
                    "message", "Job completed successfully",
                    "booking", Map.of(
                            "bookingId", booking.getBookingId(),
                            "status", booking.getStatus().name(),
                            "completedDatetime", booking.getActualEndTime() != null ? booking.getActualEndTime().toString() : ""
                    )
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
        }
    }

    private ResponseEntity<?> buildReadyToQuoteForbiddenResponse(Long transportId) {
        ReadyToQuoteStatusResponse status = rateCardService.getReadyToQuoteStatus(transportId);
        String message = status.getReason() != null && !status.getReason().isBlank()
                ? status.getReason()
                : "You must set up rate cards before accessing jobs.";
        Map<String, Object> payload = Map.of(
                "error", "PRICING_SETUP_REQUIRED",
                "message", message,
                "ready_to_quote", status.isReadyToQuote(),
                "rate_cards_count", status.getRateCardsCount(),
                "expired_cards_count", status.getExpiredCardsCount(),
                "next_expiry_at", status.getNextExpiryAt(),
                "action_url", "/transport/pricing/categories"
        );
        return ResponseEntity.status(403).body(payload);
    }

}

