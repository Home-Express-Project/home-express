package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.response.RateCardResponse;
import com.homeexpress.home_express_api.dto.response.SuggestedPriceResponse;
import com.homeexpress.home_express_api.entity.Booking;
import com.homeexpress.home_express_api.entity.BookingItem;
import com.homeexpress.home_express_api.entity.BookingStatus;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.repository.BookingItemRepository;
import com.homeexpress.home_express_api.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PricingService {

    private static final Logger log = LoggerFactory.getLogger(PricingService.class);

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final RateCardService rateCardService;

    public PricingService(BookingRepository bookingRepository,
                          BookingItemRepository bookingItemRepository,
                          RateCardService rateCardService) {
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.rateCardService = rateCardService;
    }

    @Transactional(readOnly = true)
    public SuggestedPriceResponse calculateSuggestedPrice(Long bookingId, Long transportId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        List<BookingItem> items = bookingItemRepository.findByBookingId(bookingId);
        validateBookingReadyForPricing(booking, items);

        Long primaryCategoryId = resolvePrimaryCategoryId(items);

        List<RateCardResponse> rateCards = rateCardService.getRateCardsForTransport(transportId);
        if (rateCards == null || rateCards.isEmpty()) {
            throw new IllegalStateException("No rate cards configured for this transport. Cannot calculate suggested price.");
        }

        LocalDateTime now = LocalDateTime.now();
        List<RateCardResponse> validCards = rateCards.stream()
                .filter(card -> Boolean.TRUE.equals(card.getIsActive()))
                .filter(card -> (card.getValidFrom() == null || !card.getValidFrom().isAfter(now))
                        && (card.getValidUntil() == null || card.getValidUntil().isAfter(now)))
                .collect(Collectors.toList());

        if (validCards.isEmpty()) {
            throw new IllegalStateException("No active, non-expired rate card configured for this transport.");
        }

        RateCardResponse selected = null;
        if (primaryCategoryId != null) {
            selected = validCards.stream()
                    .filter(card -> Objects.equals(primaryCategoryId, card.getCategoryId()))
                    .findFirst()
                    .orElse(null);
        }
        if (selected == null) {
            selected = validCards.get(0);
        }

        BigDecimal distanceKm = booking.getDistanceKm() != null ? booking.getDistanceKm() : BigDecimal.ZERO;
        int itemCount = items != null ? items.stream()
                .map(item -> item.getQuantity() != null ? item.getQuantity() : 1)
                .reduce(0, Integer::sum) : 0;

        int durationMinutes = estimateDurationMinutes(distanceKm.doubleValue(), itemCount);
        BigDecimal estimatedHours = BigDecimal.valueOf(durationMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        BigDecimal basePrice = defaultZero(selected.getBasePrice());
        BigDecimal distancePrice = defaultZero(selected.getPricePerKm())
                .multiply(distanceKm)
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal timePrice = defaultZero(selected.getPricePerHour())
                .multiply(estimatedHours)
                .setScale(0, RoundingMode.HALF_UP);

        Map<String, BigDecimal> appliedMultipliers = new HashMap<>();
        BigDecimal multiplier = BigDecimal.ONE;
        Map<String, BigDecimal> rules = selected.getAdditionalRules();
        if (rules != null && !rules.isEmpty() && items != null && !items.isEmpty()) {
            boolean hasFragile = items.stream().anyMatch(i -> Boolean.TRUE.equals(i.getIsFragile()));
            boolean hasDisassembly = items.stream().anyMatch(i -> Boolean.TRUE.equals(i.getRequiresDisassembly()));
            BigDecimal heavyThreshold = BigDecimal.valueOf(80);
            boolean hasHeavy = items.stream().anyMatch(i -> i.getWeightKg() != null && i.getWeightKg().compareTo(heavyThreshold) > 0);

            BigDecimal fragileMultiplier = rules.get("fragile_multiplier");
            if (hasFragile && fragileMultiplier != null) {
                multiplier = multiplier.multiply(fragileMultiplier);
                appliedMultipliers.put("fragile_multiplier", fragileMultiplier);
            }

            BigDecimal disassemblyMultiplier = rules.get("disassembly_multiplier");
            if (hasDisassembly && disassemblyMultiplier != null) {
                multiplier = multiplier.multiply(disassemblyMultiplier);
                appliedMultipliers.put("disassembly_multiplier", disassemblyMultiplier);
            }

            BigDecimal heavyMultiplier = rules.get("heavy_item_multiplier");
            if (hasHeavy && heavyMultiplier != null) {
                multiplier = multiplier.multiply(heavyMultiplier);
                appliedMultipliers.put("heavy_item_multiplier", heavyMultiplier);
            }
        }

        BigDecimal subtotal = basePrice.add(distancePrice).add(timePrice);
        BigDecimal subtotalWithMultiplier = subtotal.multiply(multiplier);
        BigDecimal roundedSubtotal = subtotalWithMultiplier.setScale(0, RoundingMode.HALF_UP);

        BigDecimal minimumCharge = defaultZero(selected.getMinimumCharge());
        boolean minimumChargeApplied = false;
        BigDecimal suggestedTotal = roundedSubtotal;
        if (minimumCharge.compareTo(BigDecimal.ZERO) > 0 && roundedSubtotal.compareTo(minimumCharge) < 0) {
            suggestedTotal = minimumCharge;
            minimumChargeApplied = true;
        }

        SuggestedPriceResponse.PriceBreakdown breakdown = new SuggestedPriceResponse.PriceBreakdown();
        breakdown.setBasePrice(basePrice);
        breakdown.setDistancePrice(distancePrice);
        breakdown.setTimePrice(timePrice);
        breakdown.setMultipliers(appliedMultipliers);
        breakdown.setMinimumChargeApplied(minimumChargeApplied);

        SuggestedPriceResponse response = new SuggestedPriceResponse();
        response.setSuggestedPrice(suggestedTotal);
        response.setPriceBreakdown(breakdown);
        response.setRateCardId(selected.getRateCardId());
        response.setCategoryId(selected.getCategoryId());
        response.setCalculationTimestamp(LocalDateTime.now());

        log.debug("Calculated suggested price {} for booking {} and transport {} using rate card {}", suggestedTotal, bookingId, transportId, selected.getRateCardId());

        return response;
    }

    private void validateBookingReadyForPricing(Booking booking, List<BookingItem> items) {
        EnumSet<BookingStatus> allowedStatuses = EnumSet.of(BookingStatus.PENDING, BookingStatus.QUOTED);
        if (!allowedStatuses.contains(booking.getStatus())) {
            throw new IllegalStateException("Booking is not ready for pricing. Current status: " + booking.getStatus());
        }
        if (booking.getPickupAddress() == null || booking.getPickupAddress().isBlank()
                || booking.getDeliveryAddress() == null || booking.getDeliveryAddress().isBlank()) {
            throw new IllegalStateException("Booking is missing pickup or delivery address information");
        }
        if (booking.getPreferredDate() == null) {
            throw new IllegalStateException("Booking is missing preferred move date");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Booking has no inventory items. Complete intake before requesting quotations.");
        }
    }

    private Long resolvePrimaryCategoryId(List<BookingItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        for (BookingItem item : items) {
            if (item.getCategoryId() != null) {
                return item.getCategoryId();
            }
        }
        return null;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private int estimateDurationMinutes(double distanceKm, int itemCount) {
        int travel = (int) Math.round(Math.max(distanceKm, 1.0) / 28.0 * 60.0);
        int handling = itemCount * 10;
        int buffer = 20;
        return Math.max(45, travel + handling + buffer);
    }
}

