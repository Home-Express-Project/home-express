package com.homeexpress.home_express_api.controller.customer;

import com.homeexpress.home_express_api.dto.request.CreateCounterOfferRequest;
import com.homeexpress.home_express_api.dto.request.RespondToCounterOfferRequest;
import com.homeexpress.home_express_api.dto.response.CounterOfferResponse;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.service.CounterOfferService;
import com.homeexpress.home_express_api.util.AuthenticationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for customer quotation and counter-offer operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customer/quotations")
@RequiredArgsConstructor
public class CustomerQuotationController {

    private final CounterOfferService counterOfferService;
    private final UserRepository userRepository;

    /**
     * Create a counter-offer for a quotation
     * POST /api/v1/customer/quotations/{quotationId}/counter-offers
     */
    @PostMapping("/{quotationId}/counter-offers")
    public ResponseEntity<?> createCounterOffer(
            @PathVariable Long quotationId,
            @Valid @RequestBody CreateCounterOfferRequest request,
            Authentication authentication) {

        User user = AuthenticationUtils.getUser(authentication, userRepository);

        // Set quotation ID from path
        request.setQuotationId(quotationId);

        CounterOfferResponse response = counterOfferService.createCounterOffer(
                request,
                user.getUserId(),
                user.getRole()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Counter-offer created successfully",
                "counterOffer", response
        ));
    }

    /**
     * Get all counter-offers for a quotation
     * GET /api/v1/customer/quotations/{quotationId}/counter-offers
     */
    @GetMapping("/{quotationId}/counter-offers")
    public ResponseEntity<?> getCounterOffersByQuotation(
            @PathVariable Long quotationId,
            Authentication authentication) {

        User user = AuthenticationUtils.getUser(authentication, userRepository);

        List<CounterOfferResponse> counterOffers = counterOfferService.getCounterOffersByQuotation(
                quotationId,
                user.getUserId(),
                user.getRole()
        );

        return ResponseEntity.ok(Map.of(
                "quotationId", quotationId,
                "counterOffers", counterOffers,
                "count", counterOffers.size()
        ));
    }

    /**
     * Get a single counter-offer by ID
     * GET /api/v1/customer/counter-offers/{counterOfferId}
     */
    @GetMapping("/counter-offers/{counterOfferId}")
    public ResponseEntity<?> getCounterOfferById(
            @PathVariable Long counterOfferId,
            Authentication authentication) {

        User user = AuthenticationUtils.getUser(authentication, userRepository);

        CounterOfferResponse response = counterOfferService.getCounterOfferById(
                counterOfferId,
                user.getUserId(),
                user.getRole()
        );

        return ResponseEntity.ok(Map.of(
                "counterOffer", response
        ));
    }

    /**
     * Respond to a counter-offer (accept or reject)
     * POST /api/v1/customer/counter-offers/{counterOfferId}/respond
     */
    @PostMapping("/counter-offers/{counterOfferId}/respond")
    public ResponseEntity<?> respondToCounterOffer(
            @PathVariable Long counterOfferId,
            @Valid @RequestBody RespondToCounterOfferRequest request,
            Authentication authentication) {

        User user = AuthenticationUtils.getUser(authentication, userRepository);

        CounterOfferResponse response = counterOfferService.respondToCounterOffer(
                counterOfferId,
                request,
                user.getUserId(),
                user.getRole()
        );

        String action = request.getAccept() ? "accepted" : "rejected";
        return ResponseEntity.ok(Map.of(
                "message", "Counter-offer " + action + " successfully",
                "counterOffer", response
        ));
    }
}

