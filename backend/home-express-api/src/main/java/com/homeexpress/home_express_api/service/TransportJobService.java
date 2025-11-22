package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.response.SuggestedPriceResponse;
import com.homeexpress.home_express_api.dto.transport.TransportActiveJobDetailDto;
import com.homeexpress.home_express_api.dto.transport.TransportActiveJobSummaryDto;
import com.homeexpress.home_express_api.dto.transport.TransportAvailableBookingDto;
import com.homeexpress.home_express_api.dto.transport.TransportPaginatedResponse;
import com.homeexpress.home_express_api.entity.*;
import com.homeexpress.home_express_api.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransportJobService {

    private static final Logger log = LoggerFactory.getLogger(TransportJobService.class);
    private static final List<BookingStatus> AVAILABLE_BOOKING_STATUSES = List.of(BookingStatus.PENDING, BookingStatus.QUOTED);
    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES = List.of(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS);

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final QuotationRepository quotationRepository;
    private final BookingStatusHistoryRepository statusHistoryRepository;
    private final CustomerRepository customerRepository;
    private final TransportRepository transportRepository;
    private final TransportSettingsRepository transportSettingsRepository;
    private final PricingService pricingService;

    public TransportJobService(
            BookingRepository bookingRepository,
            BookingItemRepository bookingItemRepository,
            QuotationRepository quotationRepository,
            BookingStatusHistoryRepository statusHistoryRepository,
            CustomerRepository customerRepository,
            TransportRepository transportRepository,
            TransportSettingsRepository transportSettingsRepository,
            PricingService pricingService
    ) {
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.quotationRepository = quotationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.customerRepository = customerRepository;
        this.transportRepository = transportRepository;
        this.transportSettingsRepository = transportSettingsRepository;
        this.pricingService = pricingService;
    }

    @Transactional(readOnly = true)
    public TransportPaginatedResponse<TransportAvailableBookingDto> getAvailableBookings(Long transportId, int page, int limit, Integer maxDistance, LocalDate preferredDate) {
        // Gate: Check if transport is APPROVED (READY_TO_QUOTE status)
        Transport transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new IllegalArgumentException("Transport not found"));

        if (transport.getVerificationStatus() != VerificationStatus.APPROVED) {
            // Return empty list if not approved - transport not ready to accept jobs
            return emptyResponse(page, limit);
        }

        // Load transport settings for filtering
        TransportSettings settings = transportSettingsRepository.findById(transportId)
                .orElse(null);

        List<Booking> bookings = bookingRepository.findByStatusInAndTransportIdIsNullOrderByCreatedAtDesc(AVAILABLE_BOOKING_STATUSES);

        if (preferredDate != null) {
            bookings = bookings.stream()
                    .filter(b -> preferredDate.equals(b.getPreferredDate()))
                    .collect(Collectors.toList());
        }

        List<TransportAvailableBookingDto> mapped = bookings.stream()
                .map(booking -> toAvailableBookingDto(booking, transportId))
                .filter(dto -> maxDistance == null || dto.getDistanceKm() == null || dto.getDistanceKm() <= maxDistance)
                // Apply settings filters: service radius and minimum job value
                .filter(dto -> settings == null || isBookingAcceptable(dto, settings))
                .collect(Collectors.toList());

        TransportPaginatedResponse<TransportAvailableBookingDto> response = new TransportPaginatedResponse<>();
        TransportPaginatedResponse.Pagination pagination = new TransportPaginatedResponse.Pagination();
        pagination.setItemsPerPage(limit);

        int totalItems = mapped.size();
        pagination.setTotalItems(totalItems);

        int totalPages = (int) Math.ceil(totalItems / (double) limit);
        pagination.setTotalPages(Math.max(totalPages, 1));
        pagination.setCurrentPage(Math.min(Math.max(page, 1), pagination.getTotalPages()));

        int fromIndex = (pagination.getCurrentPage() - 1) * limit;
        int toIndex = Math.min(fromIndex + limit, totalItems);

        if (fromIndex >= totalItems) {
            response.setData(List.of());
        } else {
            response.setData(mapped.subList(fromIndex, toIndex));
        }
        response.setPagination(pagination);
        return response;
    }

    @Transactional(readOnly = true)
    public List<TransportActiveJobSummaryDto> getActiveJobs(Long transportId) {
        List<Booking> jobs = bookingRepository.findByTransportIdAndStatusInOrderByPreferredDateAsc(transportId, ACTIVE_BOOKING_STATUSES);

        return jobs.stream()
                .map(this::toActiveJobSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TransportActiveJobDetailDto> getAvailableBookingDetail(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .filter(booking -> AVAILABLE_BOOKING_STATUSES.contains(booking.getStatus()) && booking.getTransportId() == null)
                .map(this::toActiveJobDetailDto);
    }

    @Transactional(readOnly = true)
    public Optional<TransportActiveJobDetailDto> getActiveJobDetail(Long bookingId, Long transportId) {
        return bookingRepository.findById(bookingId)
                .filter(booking -> booking.getTransportId() != null && booking.getTransportId().equals(transportId))
                .map(this::toActiveJobDetailDto);
    }

    private TransportAvailableBookingDto toAvailableBookingDto(Booking booking, Long transportId) {
        List<BookingItem> items = bookingItemRepository.findByBookingId(booking.getBookingId());

        TransportAvailableBookingDto dto = new TransportAvailableBookingDto();
        dto.setBookingId(booking.getBookingId());
        dto.setPickupLocation(booking.getPickupAddress());
        dto.setDeliveryLocation(booking.getDeliveryAddress());
        dto.setDistanceKm(toDouble(booking.getDistanceKm()));
        dto.setDistanceFromMe(null); // Requires transport geolocation support
        dto.setItemsCount(items.stream().map(BookingItem::getQuantity).filter(q -> q != null).mapToInt(Integer::intValue).sum());
        dto.setTotalWeight(items.stream()
                .map(item -> {
                    if (item.getWeightKg() == null || item.getQuantity() == null) {
                        return 0d;
                    }
                    return item.getWeightKg().doubleValue() * item.getQuantity();
                })
                .reduce(0d, Double::sum));
        dto.setHasFragileItems(items.stream().anyMatch(item -> Boolean.TRUE.equals(item.getIsFragile())));
        dto.setPreferredDate(booking.getPreferredDate() != null ? booking.getPreferredDate().toString() : null);
        dto.setPreferredTimeSlot(booking.getPreferredTimeSlot() != null ? booking.getPreferredTimeSlot().name() : null);
        dto.setEstimatedPrice(booking.getEstimatedPrice() != null ? booking.getEstimatedPrice().longValue() : 0L);
        dto.setQuotationsCount((int) quotationRepository.countByBookingId(booking.getBookingId()));
        dto.setHasQuoted(transportId != null && quotationRepository.existsByBookingIdAndTransportId(booking.getBookingId(), transportId));
        
        // Calculate suggested price using PricingService
        Long suggestedPrice = null;
        try {
            SuggestedPriceResponse rsp = pricingService.calculateSuggestedPrice(booking.getBookingId(), transportId);
            if (rsp != null && rsp.getSuggestedPrice() != null) {
                suggestedPrice = rsp.getSuggestedPrice().longValue();
            }
        } catch (Exception e) {
            log.warn("Failed to calculate suggested price for booking {} and transport {}: {}", 
                    booking.getBookingId(), transportId, e.getMessage());
        }
        dto.setSuggestedPrice(suggestedPrice);
        
        dto.setExpiresAt(calculateExpiry(booking.getPreferredDate(), booking.getCreatedAt()));
        dto.setNotifiedAt(formatDateTime(booking.getCreatedAt()));
        return dto;
    }

    private TransportActiveJobSummaryDto toActiveJobSummaryDto(Booking booking) {
        TransportActiveJobSummaryDto dto = new TransportActiveJobSummaryDto();
        dto.setBookingId(booking.getBookingId());
        dto.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        dto.setPickupAddress(booking.getPickupAddress());
        dto.setDeliveryAddress(booking.getDeliveryAddress());
        dto.setPreferredDate(booking.getPreferredDate() != null ? booking.getPreferredDate().toString() : null);
        dto.setPreferredTimeSlot(booking.getPreferredTimeSlot() != null ? booking.getPreferredTimeSlot().name() : null);
        dto.setFinalPrice(booking.getFinalPrice() != null ? booking.getFinalPrice().longValue() : null);
        dto.setDistanceKm(toDouble(booking.getDistanceKm()));
        return dto;
    }

    private TransportActiveJobDetailDto toActiveJobDetailDto(Booking booking) {
        TransportActiveJobDetailDto dto = new TransportActiveJobDetailDto();
        dto.setBookingId(booking.getBookingId());
        dto.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        dto.setPreferredDate(booking.getPreferredDate() != null ? booking.getPreferredDate().toString() : null);
        dto.setPreferredTimeSlot(booking.getPreferredTimeSlot() != null ? booking.getPreferredTimeSlot().name() : null);
        dto.setFinalPrice(booking.getFinalPrice() != null ? booking.getFinalPrice().longValue() : null);
        dto.setDistanceKm(toDouble(booking.getDistanceKm()));

        dto.setPickupAddress(booking.getPickupAddress());
        dto.setDeliveryAddress(booking.getDeliveryAddress());

        Customer customer = booking.getCustomerId() != null
                ? customerRepository.findById(booking.getCustomerId()).orElse(null)
                : null;
        String customerName = customer != null ? customer.getFullName() : null;
        String customerPhone = customer != null ? customer.getPhone() : null;

        dto.setPickupContactName(customerName);
        dto.setPickupContactPhone(customerPhone);
        dto.setDeliveryContactName(customerName);
        dto.setDeliveryContactPhone(customerPhone);

        List<BookingItem> items = bookingItemRepository.findByBookingId(booking.getBookingId());
        dto.setItems(items.stream().map(this::toJobItem).collect(Collectors.toList()));

        List<BookingStatusHistory> history = statusHistoryRepository.findByBookingIdOrderByChangedAtAsc(booking.getBookingId());
        dto.setStatusHistory(history.stream().map(this::toJobStatusHistory).collect(Collectors.toList()));

        return dto;
    }

    private TransportActiveJobDetailDto.JobItem toJobItem(BookingItem item) {
        TransportActiveJobDetailDto.JobItem dto = new TransportActiveJobDetailDto.JobItem();
        dto.setName(item.getName());
        dto.setQuantity(item.getQuantity());
        dto.setWeight(item.getWeightKg() != null ? item.getWeightKg().doubleValue() : null);
        dto.setIsFragile(Boolean.TRUE.equals(item.getIsFragile()));
        dto.setRequiresDisassembly(Boolean.TRUE.equals(item.getRequiresDisassembly()));
        dto.setRequiresPackaging(Boolean.FALSE); // packaging metadata not available yet
        return dto;
    }

    private TransportActiveJobDetailDto.JobStatusHistory toJobStatusHistory(BookingStatusHistory history) {
        TransportActiveJobDetailDto.JobStatusHistory dto = new TransportActiveJobDetailDto.JobStatusHistory();
        dto.setStatus(history.getNewStatus() != null ? history.getNewStatus().name() : null);
        dto.setChangedAt(formatDateTime(history.getChangedAt()));
        dto.setChangedByRole(history.getChangedByRole() != null ? history.getChangedByRole().name() : null);
        dto.setNote(history.getReason());
        return dto;
    }

    private Double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }

    private String calculateExpiry(LocalDate preferredDate, LocalDateTime createdAt) {
        LocalDateTime base = preferredDate != null
                ? preferredDate.atTime(23, 59)
                : createdAt != null ? createdAt.plusDays(7) : null;
        return formatDateTime(base);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.atZone(ZoneOffset.systemDefault()).toInstant().toString() : null;
    }

    private TransportPaginatedResponse<TransportAvailableBookingDto> emptyResponse(int page, int limit) {
        TransportPaginatedResponse<TransportAvailableBookingDto> response = new TransportPaginatedResponse<>();
        TransportPaginatedResponse.Pagination pagination = new TransportPaginatedResponse.Pagination();
        pagination.setItemsPerPage(limit);
        pagination.setTotalItems(0);
        pagination.setTotalPages(0);
        pagination.setCurrentPage(Math.min(Math.max(page, 1), 1));
        response.setData(List.of());
        response.setPagination(pagination);
        return response;
    }

    private boolean isBookingAcceptable(TransportAvailableBookingDto dto, TransportSettings settings) {
        // Check service radius
        if (settings.getSearchRadiusKm() != null && dto.getDistanceKm() != null) {
            if (dto.getDistanceKm() > settings.getSearchRadiusKm().doubleValue()) {
                return false;
            }
        }

        // Check minimum job value
        if (settings.getMinJobValueVnd() != null && settings.getMinJobValueVnd() > 0L) {
            long estimatedPrice = dto.getEstimatedPrice() != null ? dto.getEstimatedPrice() : 0L;
            if (estimatedPrice < settings.getMinJobValueVnd()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Start job - CONFIRMED → IN_PROGRESS
     * Only the assigned transport can start their job
     */
    @Transactional
    public Booking startJob(Long bookingId, Long transportId) {
        // Validate booking exists
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Validate ownership
        if (booking.getTransportId() == null || !booking.getTransportId().equals(transportId)) {
            throw new IllegalStateException("You are not assigned to this booking");
        }

        // Validate status transition
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED bookings can be started. Current status: " + booking.getStatus());
        }

        // Update status and set start time
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.IN_PROGRESS);
        booking.setActualStartTime(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        // Log status history
        BookingStatusHistory history = new BookingStatusHistory(
                bookingId,
                oldStatus,
                BookingStatus.IN_PROGRESS,
                transportId,
                ActorRole.TRANSPORT
        );
        history.setReason("Transport started the job");
        statusHistoryRepository.save(history);

        log.info("Job started: bookingId={}, transportId={}", bookingId, transportId);
        return booking;
    }

    /**
     * Complete job - IN_PROGRESS → COMPLETED
     * Only the assigned transport can complete their job
     */
    @Transactional
    public Booking completeJob(Long bookingId, Long transportId, String completionNotes, List<String> completionPhotos) {
        // Validate booking exists
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Validate ownership
        if (booking.getTransportId() == null || !booking.getTransportId().equals(transportId)) {
            throw new IllegalStateException("You are not assigned to this booking");
        }

        // Validate status transition
        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only IN_PROGRESS bookings can be completed. Current status: " + booking.getStatus());
        }

        // Update status and set end time
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setActualEndTime(LocalDateTime.now());
        
        // Optionally add completion notes to booking notes
        if (completionNotes != null && !completionNotes.isBlank()) {
            String existingNotes = booking.getNotes() != null ? booking.getNotes() + "\n\n" : "";
            booking.setNotes(existingNotes + "Completion notes: " + completionNotes);
        }
        
        booking = bookingRepository.save(booking);

        // Log status history
        BookingStatusHistory history = new BookingStatusHistory(
                bookingId,
                oldStatus,
                BookingStatus.COMPLETED,
                transportId,
                ActorRole.TRANSPORT
        );
        history.setReason("Transport completed the job" + (completionNotes != null ? ": " + completionNotes : ""));
        statusHistoryRepository.save(history);

        log.info("Job completed: bookingId={}, transportId={}", bookingId, transportId);
        return booking;
    }
}
