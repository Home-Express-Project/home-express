package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.request.CreateCounterOfferRequest;
import com.homeexpress.home_express_api.dto.request.RespondToCounterOfferRequest;
import com.homeexpress.home_express_api.dto.response.CounterOfferResponse;
import com.homeexpress.home_express_api.entity.*;
import com.homeexpress.home_express_api.exception.BadRequestException;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.exception.UnauthorizedException;
import com.homeexpress.home_express_api.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing counter-offers in price negotiation
 */
@Slf4j
@Service
public class CounterOfferService {

    @Autowired
    private CounterOfferRepository counterOfferRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private CustomerEventService customerEventService;

    /**
     * Create a new counter-offer
     */
    @Transactional
    public CounterOfferResponse createCounterOffer(
            CreateCounterOfferRequest request,
            Long userId,
            UserRole userRole) {

        // Validate quotation exists
        Quotation quotation = quotationRepository.findById(request.getQuotationId())
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with ID: " + request.getQuotationId()));

        // Validate booking exists
        Booking booking = bookingRepository.findById(quotation.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Authorization check
        CounterOfferRole offerRole;
        if (userRole == UserRole.CUSTOMER) {
            if (!booking.getCustomerId().equals(userId)) {
                throw new UnauthorizedException("You can only make counter-offers for your own bookings");
            }
            offerRole = CounterOfferRole.CUSTOMER;
        } else if (userRole == UserRole.TRANSPORT) {
            if (!quotation.getTransportId().equals(userId)) {
                throw new UnauthorizedException("You can only make counter-offers for your own quotations");
            }
            offerRole = CounterOfferRole.TRANSPORT;
        } else {
            throw new UnauthorizedException("Only customers and transport providers can make counter-offers");
        }

        // Validate quotation status - can create counter-offer for PENDING or NEGOTIATING quotations
        if (quotation.getStatus() != QuotationStatus.PENDING
                && quotation.getStatus() != QuotationStatus.NEGOTIATING) {
            throw new BadRequestException("Cannot make counter-offer for quotation with status: " + quotation.getStatus());
        }

        // Check if quotation is expired
        if (quotation.getExpiresAt() != null && LocalDateTime.now().isAfter(quotation.getExpiresAt())) {
            throw new BadRequestException("Cannot make counter-offer for expired quotation");
        }

        // Mark any existing pending counter-offers as superseded
        List<CounterOffer> existingOffers = counterOfferRepository
                .findByQuotationIdAndStatusOrderByCreatedAtDesc(quotation.getQuotationId(), CounterOfferStatus.PENDING);
        for (CounterOffer existing : existingOffers) {
            existing.setStatus(CounterOfferStatus.SUPERSEDED);
            existing.setUpdatedAt(LocalDateTime.now());
            counterOfferRepository.save(existing);
        }

        // Create new counter-offer
        CounterOffer counterOffer = new CounterOffer();
        counterOffer.setQuotationId(quotation.getQuotationId());
        counterOffer.setBookingId(booking.getBookingId());
        counterOffer.setOfferedByUserId(userId);
        counterOffer.setOfferedByRole(offerRole);
        counterOffer.setOfferedPrice(request.getOfferedPrice());
        counterOffer.setOriginalPrice(quotation.getQuotedPrice()); // Fixed: getQuotedPrice() instead of getTotalPrice()
        counterOffer.setStatus(CounterOfferStatus.PENDING);
        counterOffer.setMessage(request.getMessage());
        counterOffer.setReason(request.getReason());

        // Set expiration
        int expirationHours = request.getExpirationHours() != null ? request.getExpirationHours() : 24;
        counterOffer.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));

        CounterOffer saved = counterOfferRepository.save(counterOffer);

        // Update quotation status to NEGOTIATING to indicate active negotiation
        quotation.setStatus(QuotationStatus.NEGOTIATING);
        quotationRepository.save(quotation);

        // Send SSE event
        try {
            customerEventService.sendCounterOfferCreated(booking.getBookingId(), saved.getCounterOfferId());
        } catch (Exception e) {
            log.error("Failed to send counter-offer created event", e);
        }

        log.info("Counter-offer created: ID={}, QuotationID={}, OfferedBy={}, Price={}",
                saved.getCounterOfferId(), quotation.getQuotationId(), userId, request.getOfferedPrice());

        return buildCounterOfferResponse(saved);
    }

    /**
     * Respond to a counter-offer (accept or reject)
     */
    @Transactional
    public CounterOfferResponse respondToCounterOffer(
            Long counterOfferId,
            RespondToCounterOfferRequest request,
            Long userId,
            UserRole userRole) {

        // Find counter-offer
        CounterOffer counterOffer = counterOfferRepository.findById(counterOfferId)
                .orElseThrow(() -> new ResourceNotFoundException("Counter-offer not found with ID: " + counterOfferId));

        // Validate quotation and booking
        Quotation quotation = quotationRepository.findById(counterOffer.getQuotationId())
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        Booking booking = bookingRepository.findById(counterOffer.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Authorization check - responder must be the opposite party
        if (counterOffer.getOfferedByRole() == CounterOfferRole.CUSTOMER) {
            // Customer made the offer, transport must respond
            if (userRole != UserRole.TRANSPORT || !quotation.getTransportId().equals(userId)) {
                throw new UnauthorizedException("Only the transport provider can respond to this counter-offer");
            }
        } else {
            // Transport made the offer, customer must respond
            if (userRole != UserRole.CUSTOMER || !booking.getCustomerId().equals(userId)) {
                throw new UnauthorizedException("Only the customer can respond to this counter-offer");
            }
        }

        // Validate counter-offer can be responded to
        if (!counterOffer.canBeRespondedTo()) {
            throw new BadRequestException("Counter-offer cannot be responded to. Status: " + counterOffer.getStatus());
        }

        // Update counter-offer
        counterOffer.setRespondedByUserId(userId);
        counterOffer.setRespondedAt(LocalDateTime.now());
        counterOffer.setResponseMessage(request.getResponseMessage());

        if (request.getAccept()) {
            counterOffer.setStatus(CounterOfferStatus.ACCEPTED);

            // Update quotation price - Fixed: setQuotedPrice() instead of setTotalPrice()
            quotation.setQuotedPrice(counterOffer.getOfferedPrice());
            quotation.setStatus(QuotationStatus.PENDING); // Back to pending for customer to accept
            quotationRepository.save(quotation);

            log.info("Counter-offer accepted: ID={}, NewPrice={}", counterOfferId, counterOffer.getOfferedPrice());
        } else {
            counterOffer.setStatus(CounterOfferStatus.REJECTED);
            
            // Check if there are any other pending counter-offers
            List<CounterOffer> pendingOffers = counterOfferRepository
                    .findByQuotationIdAndStatusOrderByCreatedAtDesc(
                            counterOffer.getQuotationId(),
                            CounterOfferStatus.PENDING
                    );
            
            if (pendingOffers == null || pendingOffers.isEmpty()) {
                // No more pending offers - return quotation to PENDING status
                quotation.setStatus(QuotationStatus.PENDING);
            } else {
                // Still have pending offers - keep NEGOTIATING status
                quotation.setStatus(QuotationStatus.NEGOTIATING);
            }
            quotationRepository.save(quotation);
            
            log.info("Counter-offer rejected: ID={}", counterOfferId);
        }

        CounterOffer updated = counterOfferRepository.save(counterOffer);

        // Send SSE event
        try {
            if (request.getAccept()) {
                customerEventService.sendCounterOfferAccepted(booking.getBookingId(), counterOfferId);
            } else {
                customerEventService.sendCounterOfferRejected(booking.getBookingId(), counterOfferId);
            }
        } catch (Exception e) {
            log.error("Failed to send counter-offer response event", e);
        }

        return buildCounterOfferResponse(updated);
    }

    /**
     * Get all counter-offers for a quotation
     */
    @Transactional(readOnly = true)
    public List<CounterOfferResponse> getCounterOffersByQuotation(
            Long quotationId,
            Long userId,
            UserRole userRole) {

        // Validate quotation exists
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with ID: " + quotationId));

        // Validate booking exists
        Booking booking = bookingRepository.findById(quotation.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Authorization check
        if (userRole == UserRole.CUSTOMER && !booking.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("You can only view counter-offers for your own bookings");
        } else if (userRole == UserRole.TRANSPORT && !quotation.getTransportId().equals(userId)) {
            throw new UnauthorizedException("You can only view counter-offers for your own quotations");
        }

        List<CounterOffer> counterOffers = counterOfferRepository.findByQuotationIdOrderByCreatedAtDesc(quotationId);

        return counterOffers.stream()
                .map(this::buildCounterOfferResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a single counter-offer by ID
     */
    @Transactional(readOnly = true)
    public CounterOfferResponse getCounterOfferById(
            Long counterOfferId,
            Long userId,
            UserRole userRole) {

        CounterOffer counterOffer = counterOfferRepository.findById(counterOfferId)
                .orElseThrow(() -> new ResourceNotFoundException("Counter-offer not found with ID: " + counterOfferId));

        // Validate quotation and booking
        Quotation quotation = quotationRepository.findById(counterOffer.getQuotationId())
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        Booking booking = bookingRepository.findById(counterOffer.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Authorization check
        if (userRole == UserRole.CUSTOMER && !booking.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("You can only view counter-offers for your own bookings");
        } else if (userRole == UserRole.TRANSPORT && !quotation.getTransportId().equals(userId)) {
            throw new UnauthorizedException("You can only view counter-offers for your own quotations");
        }

        return buildCounterOfferResponse(counterOffer);
    }

    /**
     * Build counter-offer response with user names
     */
    private CounterOfferResponse buildCounterOfferResponse(CounterOffer counterOffer) {
        String offeredByName = getUserName(counterOffer.getOfferedByUserId(), counterOffer.getOfferedByRole());
        String respondedByName = counterOffer.getRespondedByUserId() != null
                ? getUserName(counterOffer.getRespondedByUserId(),
                        counterOffer.getOfferedByRole() == CounterOfferRole.CUSTOMER
                                ? CounterOfferRole.TRANSPORT
                                : CounterOfferRole.CUSTOMER)
                : null;

        return CounterOfferResponse.fromEntity(counterOffer, offeredByName, respondedByName);
    }

    /**
     * Get user name based on role
     */
    private String getUserName(Long userId, CounterOfferRole role) {
        if (role == CounterOfferRole.CUSTOMER) {
            return customerRepository.findById(userId)
                    .map(Customer::getFullName)
                    .orElse("Unknown Customer");
        } else {
            return transportRepository.findById(userId)
                    .map(Transport::getCompanyName)
                    .orElse("Unknown Transport");
        }
    }
}

