package com.homeexpress.home_express_api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.homeexpress.home_express_api.dto.estimation.AutoEstimationRequest;
import com.homeexpress.home_express_api.dto.estimation.AutoEstimationResponse;
import com.homeexpress.home_express_api.dto.estimation.AutoEstimationResponse.Breakdown;
import com.homeexpress.home_express_api.dto.estimation.AutoEstimationResponse.PriceRange;
import com.homeexpress.home_express_api.dto.estimation.AutoEstimationResponse.TransportEstimate;
import com.homeexpress.home_express_api.entity.CategoryPricing;
import com.homeexpress.home_express_api.entity.Transport;
import com.homeexpress.home_express_api.entity.Vehicle;
import com.homeexpress.home_express_api.entity.VehiclePricing;
import com.homeexpress.home_express_api.entity.VehicleStatus;
import com.homeexpress.home_express_api.entity.VehicleType;
import com.homeexpress.home_express_api.entity.VerificationStatus;
import com.homeexpress.home_express_api.repository.CategoryPricingRepository;
import com.homeexpress.home_express_api.repository.TransportRepository;
import com.homeexpress.home_express_api.repository.VehiclePricingRepository;
import com.homeexpress.home_express_api.repository.VehicleRepository;

@Service
public class EstimationService {

    private static final BigDecimal DEFAULT_ITEM_WEIGHT_KG = BigDecimal.valueOf(25);
    private static final BigDecimal DEFAULT_ITEM_PRICE_VND = BigDecimal.valueOf(75_000);
    private static final BigDecimal DEFAULT_FRAGILE_MULTIPLIER = BigDecimal.valueOf(1.20);
    private static final BigDecimal DEFAULT_DISASSEMBLY_MULTIPLIER = BigDecimal.valueOf(1.30);
    private static final BigDecimal DEFAULT_HEAVY_MULTIPLIER = BigDecimal.valueOf(1.50);
    private static final BigDecimal DEFAULT_HEAVY_THRESHOLD_KG = BigDecimal.valueOf(120);
    private static final BigDecimal DEFAULT_PACKAGING_FEE = BigDecimal.valueOf(20_000);
    private static final int DEFAULT_NO_ELEVATOR_THRESHOLD = 3;
    private static final BigDecimal DEFAULT_NO_ELEVATOR_FEE_PER_FLOOR = BigDecimal.valueOf(35_000);
    private static final BigDecimal DEFAULT_PEAK_MULTIPLIER = BigDecimal.valueOf(1.15);
    private static final BigDecimal DEFAULT_WEEKEND_MULTIPLIER = BigDecimal.valueOf(1.10);
    private static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int MAX_RESULTS = 5;

    private final TransportRepository transportRepository;
    private final VehicleRepository vehicleRepository;
    private final VehiclePricingRepository vehiclePricingRepository;
    private final CategoryPricingRepository categoryPricingRepository;

    public EstimationService(
            TransportRepository transportRepository,
            VehicleRepository vehicleRepository,
            VehiclePricingRepository vehiclePricingRepository,
            CategoryPricingRepository categoryPricingRepository) {
        this.transportRepository = transportRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehiclePricingRepository = vehiclePricingRepository;
        this.categoryPricingRepository = categoryPricingRepository;
    }

    public AutoEstimationResponse generateAutoEstimation(AutoEstimationRequest request) {
        AutoEstimationResponse response = new AutoEstimationResponse();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            response.setSuccess(false);
            response.setMessage("Khong co vat dung nao de tinh gia.");
            return response;
        }

        AddressComponents pickupAddress = parseAddress(request.getPickupAddress());
        AddressComponents deliveryAddress = parseAddress(request.getDeliveryAddress());

        double distanceKm = estimateDistanceKm(pickupAddress, deliveryAddress, request.getItems().size());
        BigDecimal totalWeightKg = estimateWeightKg(request);
        VehicleType recommendedType = recommendVehicleType(totalWeightKg, request.getItems().size());
        ZonedDateTime pickupDateTime = resolvePickupDateTime(request.getPickupDatetime());

        List<TransportEstimate> estimates = buildEstimates(
                request,
                distanceKm,
                totalWeightKg,
                pickupDateTime,
                recommendedType);

        if (estimates.isEmpty()) {
            estimates = buildFallbackEstimates(distanceKm, totalWeightKg, recommendedType, request.getItems().size());
        }

        estimates.sort(Comparator.comparingLong(TransportEstimate::getTotalPrice));

        response.setSuccess(!estimates.isEmpty());
        response.setDistanceKm(roundDistance(distanceKm));
        response.setEstimatedWeightKg(totalWeightKg.setScale(1, RoundingMode.HALF_UP).doubleValue());
        response.setRecommendedVehicleType(recommendedType.name());
        response.setEstimations(estimates);
        response.setPriceRange(computePriceRange(estimates));

        if (response.isSuccess()) {
            response.setMessage(String.format(Locale.getDefault(),
                    "Tim thay %d don vi van chuyen phu hop",
                    estimates.size()));
        } else {
            response.setMessage("Khong tinh duoc bao gia phu hop. Vui long thu lai sau.");
        }

        return response;
    }

    private List<TransportEstimate> buildEstimates(
            AutoEstimationRequest request,
            double distanceKm,
            BigDecimal totalWeightKg,
            ZonedDateTime pickupDateTime,
            VehicleType recommendedType) {

        List<Transport> transports = transportRepository
                .findByVerificationStatusOrderByAverageRatingDesc(VerificationStatus.APPROVED);

        if (transports.isEmpty()) {
            transports = transportRepository.findAll();
        }

        List<TransportEstimate> result = new ArrayList<>();

        for (Transport transport : transports) {
            Optional<TransportEstimate> estimate = buildEstimateForTransport(
                    transport,
                    request,
                    distanceKm,
                    totalWeightKg,
                    pickupDateTime,
                    recommendedType);

            estimate.ifPresent(result::add);

            if (result.size() >= MAX_RESULTS) {
                break;
            }
        }

        return result;
    }

    private Optional<TransportEstimate> buildEstimateForTransport(
            Transport transport,
            AutoEstimationRequest request,
            double distanceKm,
            BigDecimal totalWeightKg,
            ZonedDateTime pickupDateTime,
            VehicleType recommendedType) {

        List<Vehicle> activeVehicles = vehicleRepository
                .findByTransportTransportIdAndStatus(transport.getTransportId(), VehicleStatus.ACTIVE);

        if (activeVehicles.isEmpty()) {
            return Optional.empty();
        }

        Vehicle vehicle = selectVehicle(activeVehicles, recommendedType);
        VehiclePricingSnapshot pricing = resolvePricingSnapshot(
                transport.getTransportId(), vehicle.getType(), pickupDateTime.toLocalDateTime());

        ZonedDateTime localizedPickup = pickupDateTime.withZoneSameInstant(pricing.timezone());

        BigDecimal basePrice = pricing.basePrice();
        BigDecimal distancePrice = calculateDistancePrice(pricing, distanceKm);
        BigDecimal itemsPrice = calculateItemsPrice(transport.getTransportId(), request, localizedPickup.toLocalDateTime());
        BigDecimal floorFees = calculateFloorFees(pricing, request);

        BigDecimal subtotal = basePrice
                .add(distancePrice)
                .add(itemsPrice)
                .add(floorFees);

        if (pricing.minCharge() != null && subtotal.compareTo(pricing.minCharge()) < 0) {
            subtotal = pricing.minCharge();
        }

        BigDecimal multiplier = determineMultiplier(pricing, localizedPickup);
        BigDecimal total = subtotal
                .multiply(multiplier)
                .setScale(0, RoundingMode.HALF_UP);

        Breakdown breakdown = new Breakdown();
        breakdown.setBasePrice(basePrice.setScale(0, RoundingMode.HALF_UP).longValue());
        breakdown.setDistancePrice(distancePrice.setScale(0, RoundingMode.HALF_UP).longValue());
        breakdown.setItemsPrice(itemsPrice.setScale(0, RoundingMode.HALF_UP).longValue());
        breakdown.setFloorFees(floorFees.setScale(0, RoundingMode.HALF_UP).longValue());
        breakdown.setSubtotal(subtotal.setScale(0, RoundingMode.HALF_UP).longValue());
        breakdown.setMultiplier(multiplier.doubleValue());

        TransportEstimate estimate = new TransportEstimate();
        estimate.setTransportId(transport.getTransportId());
        estimate.setTransportName(transport.getCompanyName());
        estimate.setRating(transport.getAverageRating() != null
                ? transport.getAverageRating().setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 4.2);
        estimate.setCompletedJobs(transport.getCompletedBookings() != null ? transport.getCompletedBookings() : 0);
        estimate.setVehicleId(vehicle.getVehicleId());
        estimate.setVehicleType(vehicle.getType().name());
        estimate.setVehicleName(vehicle.getModel());
        estimate.setLicensePlate(vehicle.getLicensePlate());
        estimate.setTotalPrice(total.longValue());
        estimate.setEstimatedDuration(estimateDurationMinutes(distanceKm, request.getItems().size()));
        estimate.setRankScore(calculateRankScore(estimate, total, totalWeightKg));
        estimate.setBreakdown(breakdown);

        return Optional.of(estimate);
    }

    private Vehicle selectVehicle(List<Vehicle> vehicles, VehicleType preferredType) {
        return vehicles.stream()
                .filter(v -> v.getType() == preferredType)
                .findFirst()
                .orElseGet(() -> vehicles.stream()
                        .sorted(Comparator.comparing(Vehicle::getCapacityKg, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                        .findFirst()
                        .orElse(vehicles.get(0)));
    }

    private VehiclePricingSnapshot resolvePricingSnapshot(Long transportId, VehicleType vehicleType, LocalDateTime reference) {
        List<VehiclePricing> activePricings = vehiclePricingRepository
                .findActiveByTransportAndVehicleType(transportId, vehicleType);

        LocalDateTime checkDate = reference != null ? reference : LocalDateTime.now();

        VehiclePricing pricing = activePricings.stream()
                .filter(p -> isPricingActive(p, checkDate))
                .findFirst()
                .orElse(activePricings.stream().findFirst().orElse(null));

        if (pricing == null) {
            return VehiclePricingSnapshot.defaultSnapshot(vehicleType);
        }

        return VehiclePricingSnapshot.fromEntity(pricing);
    }

    private boolean isPricingActive(VehiclePricing pricing, LocalDateTime checkDate) {
        return (pricing.getValidFrom() == null || !checkDate.isBefore(pricing.getValidFrom()))
                && (pricing.getValidTo() == null || !checkDate.isAfter(pricing.getValidTo()))
                && Boolean.TRUE.equals(pricing.getIsActive());
    }

    private BigDecimal calculateDistancePrice(VehiclePricingSnapshot pricing, double distanceKm) {
        BigDecimal remaining = BigDecimal.valueOf(distanceKm);
        BigDecimal price = BigDecimal.ZERO;

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }

        BigDecimal firstSegment = remaining.min(BigDecimal.valueOf(4));
        price = price.add(firstSegment.multiply(pricing.perKmFirst4()));
        remaining = remaining.subtract(firstSegment);

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal secondSegment = remaining.min(BigDecimal.valueOf(36));
            price = price.add(secondSegment.multiply(pricing.perKm5To40()));
            remaining = remaining.subtract(secondSegment);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            price = price.add(remaining.multiply(pricing.perKmAfter40()));
        }

        return price.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateItemsPrice(Long transportId, AutoEstimationRequest request, LocalDateTime reference) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDateTime checkDate = reference != null ? reference : LocalDateTime.now();

        for (AutoEstimationRequest.Item item : request.getItems()) {
            CategoryPricingSnapshot snapshot = resolveCategoryPricingSnapshot(
                    transportId,
                    item.getCategoryId(),
                    checkDate);

            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
            BigDecimal itemWeight = BigDecimal.valueOf(item.getWeight() != null && item.getWeight() > 0
                    ? item.getWeight()
                    : DEFAULT_ITEM_WEIGHT_KG.doubleValue());

            BigDecimal linePrice = snapshot.pricePerUnit().multiply(quantity);
            BigDecimal multiplier = BigDecimal.ONE;

            if (Boolean.TRUE.equals(item.getFragile())) {
                multiplier = multiplier.multiply(snapshot.fragileMultiplier());
            }

            if (Boolean.TRUE.equals(item.getRequiresDisassembly())) {
                multiplier = multiplier.multiply(snapshot.disassemblyMultiplier());
            }

            if (itemWeight.compareTo(snapshot.heavyThresholdKg()) > 0) {
                multiplier = multiplier.multiply(snapshot.heavyMultiplier());
            }

            linePrice = linePrice.multiply(multiplier);

            if (Boolean.TRUE.equals(item.getRequiresPackaging())) {
                linePrice = linePrice.add(DEFAULT_PACKAGING_FEE.multiply(quantity));
            }

            total = total.add(linePrice);
        }

        return total.setScale(0, RoundingMode.HALF_UP);
    }

    private CategoryPricingSnapshot resolveCategoryPricingSnapshot(Long transportId, Long categoryId, LocalDateTime reference) {
        if (transportId == null || categoryId == null || categoryId <= 0) {
            return CategoryPricingSnapshot.defaultSnapshot();
        }

        Optional<CategoryPricing> pricing = categoryPricingRepository
                .findActiveByCategoryAndSizeAndDate(transportId, categoryId, null, reference);

        return pricing.map(CategoryPricingSnapshot::fromEntity).orElseGet(CategoryPricingSnapshot::defaultSnapshot);
    }

    private BigDecimal calculateFloorFees(VehiclePricingSnapshot pricing, AutoEstimationRequest request) {
        BigDecimal fees = BigDecimal.ZERO;

        int pickupFloor = Math.max(0, request.getPickupFloor() != null ? request.getPickupFloor() : 0);
        int deliveryFloor = Math.max(0, request.getDeliveryFloor() != null ? request.getDeliveryFloor() : 0);

        if (!Boolean.TRUE.equals(request.getHasElevatorPickup())
                && pickupFloor > pricing.noElevatorFloorThreshold()) {
            int extraFloors = pickupFloor - pricing.noElevatorFloorThreshold();
            fees = fees.add(pricing.noElevatorFeePerFloor().multiply(BigDecimal.valueOf(extraFloors)));
        }

        if (!Boolean.TRUE.equals(request.getHasElevatorDelivery())
                && deliveryFloor > pricing.noElevatorFloorThreshold()) {
            int extraFloors = deliveryFloor - pricing.noElevatorFloorThreshold();
            fees = fees.add(pricing.noElevatorFeePerFloor().multiply(BigDecimal.valueOf(extraFloors)));
        }

        return fees.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal determineMultiplier(VehiclePricingSnapshot pricing, ZonedDateTime pickup) {
        BigDecimal multiplier = BigDecimal.ONE;

        if (isWeekend(pickup)) {
            multiplier = multiplier.multiply(pricing.weekendMultiplier());
        }

        if (isDuringPeakHour(pricing, pickup)) {
            multiplier = multiplier.multiply(pricing.peakHourMultiplier());
        }

        return multiplier.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isWeekend(ZonedDateTime dateTime) {
        return switch (dateTime.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> true;
            default -> false;
        };
    }

    private boolean isDuringPeakHour(VehiclePricingSnapshot pricing, ZonedDateTime dateTime) {
        int hour = dateTime.getHour();
        return inRange(hour, pricing.peakHourStart1(), pricing.peakHourEnd1())
                || inRange(hour, pricing.peakHourStart2(), pricing.peakHourEnd2());
    }

    private boolean inRange(int hour, Integer start, Integer end) {
        if (start == null || end == null) {
            return false;
        }
        return hour >= start && hour < end;
    }

    private int estimateDurationMinutes(double distanceKm, int itemCount) {
        int travel = (int) Math.round(Math.max(distanceKm, 1.0) / 28.0 * 60.0);
        int handling = itemCount * 10;
        int buffer = 20;
        return Math.max(45, travel + handling + buffer);
    }

    private double calculateRankScore(TransportEstimate estimate, BigDecimal totalPrice, BigDecimal totalWeight) {
        double ratingScore = estimate.getRating() * 80;
        double completionScore = Math.min(estimate.getCompletedJobs(), 800) / 4.0;
        double priceScore = 700_000.0 / (totalPrice.doubleValue() + 1);
        double capacityFactor = totalWeight.doubleValue() / 100.0;
        return Math.round((ratingScore + completionScore + priceScore - capacityFactor) * 100.0) / 100.0;
    }

    private List<TransportEstimate> buildFallbackEstimates(
            double distanceKm,
            BigDecimal totalWeightKg,
            VehicleType vehicleType,
            int itemCount) {

        List<TransportEstimate> fallbacks = new ArrayList<>();

        long basePrice = Math.round(280_000 + (distanceKm * 4_500));
        long itemsPrice = itemCount * 60_000L;
        long floorFees = 40_000L;
        long subtotal = basePrice + itemsPrice + floorFees;
        long total = Math.round(subtotal * 1.08);

        for (int i = 0; i < 3; i++) {
            TransportEstimate estimate = new TransportEstimate();
            estimate.setTransportId(-100L - i);
            estimate.setTransportName(switch (i) {
                case 0 -> "HomeExpress Logistics";
                case 1 -> "Nhanh Chong Movers";
                default -> "An Tam Transport";
            });
            estimate.setRating(4.2 + (i * 0.15));
            estimate.setCompletedJobs(120 + i * 30);
            estimate.setVehicleId(null);
            estimate.setVehicleType(vehicleType.name());
            estimate.setVehicleName(vehicleType.name().toUpperCase(Locale.ROOT));
            estimate.setLicensePlate("N/A");
            estimate.setEstimatedDuration(estimateDurationMinutes(distanceKm, itemCount));
            estimate.setBreakdown(buildFallbackBreakdown(basePrice, itemsPrice, floorFees, subtotal));
            estimate.setTotalPrice(total + i * 35_000L);
            estimate.setRankScore(estimate.getRating() * 80 - i * 5);
            fallbacks.add(estimate);
        }

        return fallbacks;
    }

    private Breakdown buildFallbackBreakdown(long basePrice, long itemsPrice, long floorFees, long subtotal) {
        Breakdown breakdown = new Breakdown();
        breakdown.setBasePrice(basePrice);
        breakdown.setDistancePrice(Math.round(basePrice * 0.35));
        breakdown.setItemsPrice(itemsPrice);
        breakdown.setFloorFees(floorFees);
        breakdown.setSubtotal(subtotal);
        breakdown.setMultiplier(1.08);
        return breakdown;
    }

    private PriceRange computePriceRange(List<TransportEstimate> estimates) {
        if (estimates.isEmpty()) {
            return new PriceRange(0, 0, 0);
        }

        List<Long> totals = estimates.stream()
                .map(TransportEstimate::getTotalPrice)
                .collect(Collectors.toList());

        long lowest = totals.stream().min(Long::compareTo).orElse(0L);
        long highest = totals.stream().max(Long::compareTo).orElse(0L);
        long average = Math.round(totals.stream().mapToLong(Long::longValue).average().orElse(0.0));

        return new PriceRange(lowest, highest, average);
    }

    private BigDecimal estimateWeightKg(AutoEstimationRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        for (AutoEstimationRequest.Item item : request.getItems()) {
            BigDecimal weight = BigDecimal.valueOf(item.getWeight() != null && item.getWeight() > 0
                    ? item.getWeight()
                    : DEFAULT_ITEM_WEIGHT_KG.doubleValue());
            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
            total = total.add(weight.multiply(quantity));
        }
        return total;
    }

    private VehicleType recommendVehicleType(BigDecimal totalWeightKg, int itemCount) {
        if (totalWeightKg.compareTo(BigDecimal.valueOf(180)) <= 0 && itemCount <= 8) {
            return VehicleType.van;
        }
        if (totalWeightKg.compareTo(BigDecimal.valueOf(1_400)) <= 0) {
            return VehicleType.truck_small;
        }
        return VehicleType.truck_large;
    }

    private AddressComponents parseAddress(String combined) {
        if (!StringUtils.hasText(combined)) {
            return AddressComponents.empty();
        }

        String[] rawParts = combined.split(",");
        List<String> parts = new ArrayList<>();
        for (String part : rawParts) {
            String trimmed = part.trim();
            if (StringUtils.hasText(trimmed)) {
                parts.add(trimmed);
            }
        }

        if (parts.size() < 4) {
            return new AddressComponents(combined.trim(), null, null, null);
        }

        String province = parts.get(parts.size() - 1);
        String district = parts.get(parts.size() - 2);
        String ward = parts.get(parts.size() - 3);
        String street = parts.subList(0, parts.size() - 3).stream()
                .collect(Collectors.joining(", ")).trim();

        return new AddressComponents(street, ward, district, province);
    }

    private double estimateDistanceKm(AddressComponents pickup, AddressComponents delivery, int itemCount) {
        double base = 24 + itemCount;

        if (StringUtils.hasText(pickup.provinceCode) && pickup.provinceCode.equals(delivery.provinceCode)) {
            base = 18 + itemCount * 0.8;
            if (StringUtils.hasText(pickup.districtCode) && pickup.districtCode.equals(delivery.districtCode)) {
                base = 12 + itemCount * 0.6;
                if (StringUtils.hasText(pickup.wardCode) && pickup.wardCode.equals(delivery.wardCode)) {
                    base = 6 + itemCount * 0.4;
                }
            }
        } else if (StringUtils.hasText(pickup.provinceCode) && StringUtils.hasText(delivery.provinceCode)) {
            base = 70 + itemCount * 1.5;
        }

        return Math.max(3.0, roundDistance(base));
    }

    private double roundDistance(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private ZonedDateTime resolvePickupDateTime(String pickupDatetime) {
        if (!StringUtils.hasText(pickupDatetime)) {
            return ZonedDateTime.now(DEFAULT_TIMEZONE);
        }

        try {
            OffsetDateTime offset = OffsetDateTime.parse(pickupDatetime);
            return offset.atZoneSameInstant(DEFAULT_TIMEZONE);
        } catch (DateTimeParseException ignored) {
            try {
                LocalDateTime local = LocalDateTime.parse(pickupDatetime);
                return local.atZone(DEFAULT_TIMEZONE);
            } catch (DateTimeParseException ignoredAgain) {
                return ZonedDateTime.now(DEFAULT_TIMEZONE);
            }
        }
    }

    private record VehiclePricingSnapshot(
            VehicleType vehicleType,
            BigDecimal basePrice,
            BigDecimal perKmFirst4,
            BigDecimal perKm5To40,
            BigDecimal perKmAfter40,
            BigDecimal minCharge,
            BigDecimal noElevatorFeePerFloor,
            int noElevatorFloorThreshold,
            BigDecimal peakHourMultiplier,
            BigDecimal weekendMultiplier,
            Integer peakHourStart1,
            Integer peakHourEnd1,
            Integer peakHourStart2,
            Integer peakHourEnd2,
            ZoneId timezone) {

        static VehiclePricingSnapshot fromEntity(VehiclePricing pricing) {
            return new VehiclePricingSnapshot(
                    pricing.getVehicleType(),
                    defaultZero(pricing.getBasePriceVnd()),
                    defaultZero(pricing.getPerKmFirst4KmVnd()),
                    defaultZero(pricing.getPerKm5To40KmVnd()),
                    defaultZero(pricing.getPerKmAfter40KmVnd()),
                    pricing.getMinChargeVnd(),
                    defaultZero(pricing.getNoElevatorFeePerFloorVnd()),
                    pricing.getNoElevatorFloorThreshold() != null
                            ? pricing.getNoElevatorFloorThreshold()
                            : DEFAULT_NO_ELEVATOR_THRESHOLD,
                    pricing.getPeakHourMultiplier() != null
                            ? pricing.getPeakHourMultiplier()
                            : DEFAULT_PEAK_MULTIPLIER,
                    pricing.getWeekendMultiplier() != null
                            ? pricing.getWeekendMultiplier()
                            : DEFAULT_WEEKEND_MULTIPLIER,
                    pricing.getPeakHourStart1(),
                    pricing.getPeakHourEnd1(),
                    pricing.getPeakHourStart2(),
                    pricing.getPeakHourEnd2(),
                    resolveZone(pricing.getTimezone()));
        }

        static VehiclePricingSnapshot defaultSnapshot(VehicleType vehicleType) {
            return new VehiclePricingSnapshot(
                    vehicleType,
                    BigDecimal.valueOf(320_000),
                    BigDecimal.valueOf(23_000),
                    BigDecimal.valueOf(18_000),
                    BigDecimal.valueOf(14_000),
                    BigDecimal.valueOf(420_000),
                    DEFAULT_NO_ELEVATOR_FEE_PER_FLOOR,
                    DEFAULT_NO_ELEVATOR_THRESHOLD,
                    DEFAULT_PEAK_MULTIPLIER,
                    DEFAULT_WEEKEND_MULTIPLIER,
                    7,
                    9,
                    17,
                    19,
                    DEFAULT_TIMEZONE);
        }

        private static BigDecimal defaultZero(BigDecimal value) {
            return value != null ? value : BigDecimal.ZERO;
        }

        private static ZoneId resolveZone(String timezone) {
            if (!StringUtils.hasText(timezone)) {
                return DEFAULT_TIMEZONE;
            }
            try {
                return ZoneId.of(timezone);
            } catch (Exception ignored) {
                return DEFAULT_TIMEZONE;
            }
        }
    }

    private record CategoryPricingSnapshot(
            BigDecimal pricePerUnit,
            BigDecimal fragileMultiplier,
            BigDecimal disassemblyMultiplier,
            BigDecimal heavyMultiplier,
            BigDecimal heavyThresholdKg) {

        static CategoryPricingSnapshot fromEntity(CategoryPricing pricing) {
            return new CategoryPricingSnapshot(
                    defaultValue(pricing.getPricePerUnitVnd(), DEFAULT_ITEM_PRICE_VND),
                    defaultValue(pricing.getFragileMultiplier(), DEFAULT_FRAGILE_MULTIPLIER),
                    defaultValue(pricing.getDisassemblyMultiplier(), DEFAULT_DISASSEMBLY_MULTIPLIER),
                    defaultValue(pricing.getHeavyMultiplier(), DEFAULT_HEAVY_MULTIPLIER),
                    defaultValue(pricing.getHeavyThresholdKg(), DEFAULT_HEAVY_THRESHOLD_KG));
        }

        static CategoryPricingSnapshot defaultSnapshot() {
            return new CategoryPricingSnapshot(
                    DEFAULT_ITEM_PRICE_VND,
                    DEFAULT_FRAGILE_MULTIPLIER,
                    DEFAULT_DISASSEMBLY_MULTIPLIER,
                    DEFAULT_HEAVY_MULTIPLIER,
                    DEFAULT_HEAVY_THRESHOLD_KG);
        }

        private static BigDecimal defaultValue(BigDecimal value, BigDecimal defaultVal) {
            return value != null && value.compareTo(BigDecimal.ZERO) > 0 ? value : defaultVal;
        }
    }

    private static final class AddressComponents {
        private final String addressLine;
        private final String wardCode;
        private final String districtCode;
        private final String provinceCode;

        AddressComponents(String addressLine, String wardCode, String districtCode, String provinceCode) {
            this.addressLine = addressLine;
            this.wardCode = wardCode;
            this.districtCode = districtCode;
            this.provinceCode = provinceCode;
        }

        static AddressComponents empty() {
            return new AddressComponents(null, null, null, null);
        }
    }
}
