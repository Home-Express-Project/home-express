package com.homeexpress.home_express_api.controller.transport;

import com.homeexpress.home_express_api.dto.request.RateCardRequest;
import com.homeexpress.home_express_api.dto.response.RateCardResponse;
import com.homeexpress.home_express_api.dto.response.ReadyToQuoteStatusResponse;
import com.homeexpress.home_express_api.entity.Transport;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.repository.TransportRepository;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.service.RateCardService;
import com.homeexpress.home_express_api.util.AuthenticationUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transport")
public class TransportRateCardController {

    private final UserRepository userRepository;
    private final TransportRepository transportRepository;
    private final RateCardService rateCardService;

    public TransportRateCardController(UserRepository userRepository,
                                       TransportRepository transportRepository,
                                       RateCardService rateCardService) {
        this.userRepository = userRepository;
        this.transportRepository = transportRepository;
        this.rateCardService = rateCardService;
    }

    @GetMapping("/rate-cards")
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<?> getRateCards(Authentication authentication) {
        User user = AuthenticationUtils.getUser(authentication, userRepository);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only transport accounts can access this resource"));
        }

        Transport transport = transportRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Transport profile not found for user"));

        List<RateCardResponse> cards = rateCardService.getRateCardsForTransport(transport.getTransportId());
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/rate-cards")
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<?> createOrUpdateRateCard(@Valid @RequestBody RateCardRequest request,
                                                    Authentication authentication) {
        User user = AuthenticationUtils.getUser(authentication, userRepository);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only transport accounts can access this resource"));
        }

        Transport transport = transportRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Transport profile not found for user"));

        RateCardResponse response = rateCardService.createOrUpdateRateCard(transport.getTransportId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/rate-cards/{rateCardId}")
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<?> deleteRateCard(@PathVariable Long rateCardId,
                                            Authentication authentication) {
        User user = AuthenticationUtils.getUser(authentication, userRepository);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only transport accounts can access this resource"));
        }

        Transport transport = transportRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Transport profile not found for user"));

        rateCardService.deleteRateCard(transport.getTransportId(), rateCardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ready-status")
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<?> getReadyStatus(Authentication authentication) {
        User user = AuthenticationUtils.getUser(authentication, userRepository);
        if (user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only transport accounts can access this resource"));
        }

        Transport transport = transportRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Transport profile not found for user"));

        ReadyToQuoteStatusResponse status = rateCardService.getReadyToQuoteStatus(transport.getTransportId());
        return ResponseEntity.ok(status);
    }
}

