package com.homeexpress.home_express_api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homeexpress.home_express_api.config.PaymentConfig;
import com.homeexpress.home_express_api.dto.payment.PaymentConfirmRequestDTO;
import com.homeexpress.home_express_api.dto.payment.PaymentInitRequestDTO;
import com.homeexpress.home_express_api.dto.payment.PaymentResponseDTO;
import com.homeexpress.home_express_api.dto.payment.PaymentSummaryDTO;
import com.homeexpress.home_express_api.dto.request.InitiateDepositRequest;
import com.homeexpress.home_express_api.dto.request.InitiateRemainingPaymentRequest;
import com.homeexpress.home_express_api.dto.response.BankInfoResponse;
import com.homeexpress.home_express_api.dto.response.InitiateDepositResponse;
import com.homeexpress.home_express_api.dto.request.PaymentMethodRequest;

import com.homeexpress.home_express_api.dto.response.InitiateRemainingPaymentResponse;
import com.homeexpress.home_express_api.dto.response.PaymentStatusResponse;
import com.homeexpress.home_express_api.entity.Booking;
import com.homeexpress.home_express_api.entity.BookingSettlement;
import com.homeexpress.home_express_api.entity.BookingStatus;
import com.homeexpress.home_express_api.entity.CollectionMode;
import com.homeexpress.home_express_api.entity.Notification;
import com.homeexpress.home_express_api.entity.Payment;
import com.homeexpress.home_express_api.entity.PaymentMethod;
import com.homeexpress.home_express_api.entity.PaymentStatus;
import com.homeexpress.home_express_api.entity.PaymentType;
import com.homeexpress.home_express_api.entity.SettlementStatus;
import com.homeexpress.home_express_api.entity.TransportWallet;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.repository.BookingRepository;
import com.homeexpress.home_express_api.repository.BookingSettlementRepository;
import com.homeexpress.home_express_api.repository.PaymentRepository;
import com.homeexpress.home_express_api.repository.TransportRepository;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.constants.BookingConstants;
import com.homeexpress.home_express_api.entity.WalletTransactionReferenceType;
import com.homeexpress.home_express_api.entity.WalletTransactionType;
import com.homeexpress.home_express_api.exception.PaymentNotFoundException;
import com.homeexpress.home_express_api.exception.InvalidPaymentStatusException;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PaymentConfig paymentConfig;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private BookingSettlementRepository settlementRepository;

    @Autowired
    private CommissionService commissionService;

    @Autowired
    private CustomerEventService customerEventService;

    @Autowired
    private WalletService walletService;

    public PaymentSummaryDTO getPaymentSummary(Long bookingId, Long userId, UserRole userRole) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (userRole == UserRole.CUSTOMER && !booking.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("You can only view your own bookings");
        }

        String sql = "SELECT booking_id, customer_id, transport_id, "
                + "booking_amount_vnd, total_paid_vnd, deposit_paid_vnd, "
                + "balance_paid_vnd, total_refunded_vnd, last_paid_at, "
                + "payment_count, outstanding_vnd "
                + "FROM booking_payment_summary WHERE booking_id = ?";

        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                PaymentSummaryDTO dto = new PaymentSummaryDTO();
                dto.setBookingId(rs.getLong("booking_id"));
                dto.setCustomerId(rs.getLong("customer_id"));
                dto.setTransportId(rs.getObject("transport_id", Long.class));
                dto.setBookingAmountVnd(rs.getBigDecimal("booking_amount_vnd"));
                dto.setTotalPaidVnd(rs.getBigDecimal("total_paid_vnd"));
                dto.setDepositPaidVnd(rs.getBigDecimal("deposit_paid_vnd"));
                dto.setBalancePaidVnd(rs.getBigDecimal("balance_paid_vnd"));
                dto.setTotalRefundedVnd(rs.getBigDecimal("total_refunded_vnd"));

                java.sql.Timestamp lastPaidAt = rs.getTimestamp("last_paid_at");
                if (lastPaidAt != null) {
                    dto.setLastPaidAt(lastPaidAt.toLocalDateTime());
                }

                dto.setPaymentCount(rs.getInt("payment_count"));
                dto.setOutstandingVnd(rs.getBigDecimal("outstanding_vnd"));
                return dto;
            }
            throw new ResourceNotFoundException("Payment summary", "bookingId", bookingId);
        }, bookingId);
    }

    @Transactional
    public PaymentResponseDTO initializePayment(PaymentInitRequestDTO request, Long userId, UserRole userRole) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        if (userRole == UserRole.CUSTOMER && !booking.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("You can only create payments for your own bookings");
        }

        Optional<Payment> existingPayment = paymentRepository.findByBookingId(request.getBookingId())
                .stream()
                .filter(p -> p.getIdempotencyKey() != null && p.getIdempotencyKey().equals(request.getIdempotencyKey()))
                .findFirst();

        if (existingPayment.isPresent()) {
            return PaymentResponseDTO.fromEntity(existingPayment.get());
        }

        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setPaymentType(request.getPaymentType());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(request.getAmount());
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setBankCode(request.getBankCode());
        payment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentResponseDTO.fromEntity(savedPayment);
    }

    @Transactional
    public PaymentResponseDTO confirmPayment(PaymentConfirmRequestDTO request, Long userId, UserRole userRole) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new PaymentNotFoundException(request.getPaymentId()));

        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", payment.getBookingId()));

        if (userRole == UserRole.CUSTOMER && !booking.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("You can only confirm payments for your own bookings");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return PaymentResponseDTO.fromEntity(payment);
        }

        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new InvalidPaymentStatusException(payment.getStatus(), "confirm");
        }

        payment.setTransactionId(request.getTransactionId());
        Payment updatedPayment = markPaymentAsCompleted(payment, userId);
        handlePostCompletionSideEffects(updatedPayment, booking);

        return PaymentResponseDTO.fromEntity(updatedPayment);
    }


    /**
     * Update settlement breakdown based on all completed payments
     */
    private BookingSettlement updateSettlementPaymentBreakdown(Payment payment, Booking booking) {
        BookingSettlement settlement = ensureSettlementRecord(booking);

        // Get all completed payments for this booking
        List<Payment> completedPayments = paymentRepository.findByBookingIdAndStatus(
                payment.getBookingId(), PaymentStatus.COMPLETED);

        long depositPaid = completedPayments.stream()
                .filter(p -> PaymentType.DEPOSIT.equals(p.getPaymentType()))
                .mapToLong(p -> p.getAmount().longValue())
                .sum();

        long remainingPaid = completedPayments.stream()
                .filter(p -> PaymentType.REMAINING_PAYMENT.equals(p.getPaymentType()))
                .mapToLong(p -> p.getAmount().longValue())
                .sum();

        long tipPaid = completedPayments.stream()
                .filter(p -> PaymentType.TIP.equals(p.getPaymentType()))
                .mapToLong(p -> p.getAmount().longValue())
                .sum();

        long totalCollected = depositPaid + remainingPaid + tipPaid;

        long totalGatewayFee = completedPayments.stream()
                .filter(p -> isOnlinePayment(p.getPaymentMethod()))
                .mapToLong(p -> commissionService.calculateGatewayFee(p.getAmount().longValue()))
                .sum();

        settlement.setDepositPaidVnd(depositPaid);
        settlement.setRemainingPaidVnd(remainingPaid);
        settlement.setTipVnd(tipPaid);
        settlement.setTotalCollectedVnd(totalCollected);
        settlement.setGatewayFeeVnd(totalGatewayFee);
        settlement.setCollectionMode(resolveCollectionMode(completedPayments));

        long agreedPriceVnd = booking.getFinalPrice() != null ? booking.getFinalPrice().longValue() : 0L;
        settlement.setAgreedPriceVnd(agreedPriceVnd);
        int commissionRate = commissionService.getCommissionRateBps(booking.getTransportId());
        settlement.setCommissionRateBps(commissionRate);
        settlement.setPlatformFeeVnd(commissionService.calculatePlatformFee(agreedPriceVnd, booking.getTransportId()));

        settlementRepository.save(settlement);

        log.info("Updated settlement {} for booking {} with deposit: {} VND, remaining: {} VND, tip: {} VND",
                settlement.getSettlementId(), payment.getBookingId(), depositPaid, remainingPaid, tipPaid);

        return settlement;
    }

    private boolean isOnlinePayment(PaymentMethod method) {
        return method == PaymentMethod.BANK_TRANSFER;
    }

    /**
     * Resolve collection mode based on payment methods used
     */
    private CollectionMode resolveCollectionMode(List<Payment> payments) {
        if (payments == null || payments.isEmpty()) {
            return CollectionMode.ALL_CASH; // default
        }
        
        boolean hasCash = payments.stream().anyMatch(p -> p.getPaymentMethod() == PaymentMethod.CASH);
        boolean hasOnline = payments.stream().anyMatch(p -> p.getPaymentMethod() == PaymentMethod.BANK_TRANSFER);
        
        if (hasCash && hasOnline) {
            return CollectionMode.MIXED;
        } else if (hasOnline) {
            return CollectionMode.ALL_ONLINE;
        } else {
            return CollectionMode.ALL_CASH;
        }
    }

    /**
     * Send notification when payment is confirmed
     */
    private void sendPaymentConfirmationNotification(Payment payment) {
        try {
            Booking booking = bookingRepository.findById(payment.getBookingId())
                    .orElse(null);

            if (booking == null) {
                return;
            }

            // Notify customer
            User customerUser = userRepository.findById(booking.getCustomerId()).orElse(null);
            if (customerUser != null) {
                String title;
                String message;

                if (payment.getPaymentType() == PaymentType.DEPOSIT) {
                    title = "Thanh toán đặt cọc thành công";
                    message = String.format("Đơn hàng #%d đã được thanh toán đặt cọc thành công %.0f VND.",
                            booking.getBookingId(), payment.getAmount().doubleValue());
                } else if (payment.getPaymentType() == PaymentType.REMAINING_PAYMENT) {
                    title = "Thanh toán phần còn lại thành công";
                    message = String.format("Đơn hàng #%d đã được thanh toán phần còn lại thành công %.0f VND.",
                            booking.getBookingId(), payment.getAmount().doubleValue());
                } else {
                    title = "Thanh toán thành công";
                    message = String.format("Đơn hàng #%d đã được thanh toán thành công %.0f VND.",
                            booking.getBookingId(), payment.getAmount().doubleValue());
                }

                notificationService.createNotification(
                        customerUser.getUserId(),
                        Notification.NotificationType.PAYMENT_REMINDER,
                        title,
                        message,
                        Notification.ReferenceType.PAYMENT,
                        payment.getPaymentId(),
                        Notification.Priority.HIGH
                );
            }

            // Notify transport if assigned
            if (booking.getTransportId() != null) {
                transportRepository.findById(booking.getTransportId()).ifPresent(transport -> {
                    if (transport.getUser() != null) {
                        String title;
                        String message;

                        if (payment.getPaymentType() == PaymentType.DEPOSIT) {
                            title = "Nhận được tiền đặt cọc";
                            message = String.format("Đơn hàng #%d đã nhận được tiền đặt cọc %.0f VND.",
                                    booking.getBookingId(), payment.getAmount().doubleValue());
                        } else if (payment.getPaymentType() == PaymentType.REMAINING_PAYMENT) {
                            title = "Nhận được thanh toán phần còn lại";
                            message = String.format("Đơn hàng #%d đã nhận được thanh toán phần còn lại %.0f VND.",
                                    booking.getBookingId(), payment.getAmount().doubleValue());
                        } else {
                            title = "Nhận được thanh toán";
                            message = String.format("Đơn hàng #%d đã nhận được thanh toán %.0f VND.",
                                    booking.getBookingId(), payment.getAmount().doubleValue());
                        }

                        notificationService.createNotification(
                                transport.getUser().getUserId(),
                                Notification.NotificationType.PAYMENT_REMINDER,
                                title,
                                message,
                                Notification.ReferenceType.PAYMENT,
                                payment.getPaymentId(),
                                Notification.Priority.MEDIUM
                        );
                    }
                });
            }

            // Send SSE event for real-time updates
            customerEventService.sendPaymentUpdate(
                    booking.getBookingId(),
                    payment.getPaymentId(),
                    payment.getPaymentType().name(),
                    payment.getAmount().longValue(),
                    payment.getStatus().name()
            );

            log.debug("Sent SSE payment update event for booking {}, payment {}",
                    booking.getBookingId(), payment.getPaymentId());

        } catch (Exception e) {
            // Log error but don't fail the transaction
            log.error("Failed to send payment confirmation notification: {}", e.getMessage());
        }
    }

    private BookingSettlement ensureSettlementRecord(Booking booking) {
        return settlementRepository.findByBookingId(booking.getBookingId())
                .orElseGet(() -> {
                    BookingSettlement settlement = new BookingSettlement();
                    settlement.setBookingId(booking.getBookingId());
                    settlement.setTransportId(booking.getTransportId());
                    long agreedPrice = booking.getFinalPrice() != null ? booking.getFinalPrice().longValue() : 0L;
                    settlement.setAgreedPriceVnd(agreedPrice);
                    settlement.setDepositPaidVnd(0L);
                    settlement.setRemainingPaidVnd(0L);
                    settlement.setTipVnd(0L);
                    settlement.setTotalCollectedVnd(0L);
                    settlement.setGatewayFeeVnd(0L);
                    settlement.setAdjustmentVnd(0L);
                    settlement.setCollectionMode(CollectionMode.ALL_ONLINE);
                    int commissionRate = commissionService.getCommissionRateBps(booking.getTransportId());
                    settlement.setCommissionRateBps(commissionRate);
                    settlement.setPlatformFeeVnd(commissionService.calculatePlatformFee(agreedPrice, booking.getTransportId()));
                    settlement.setStatus(SettlementStatus.PENDING);
                    return settlementRepository.save(settlement);
                });
    }

    private void finalizeSettlementIfEligible(Booking booking, BookingSettlement settlement) {
        Long totalCollected = settlement.getTotalCollectedVnd();
        long finalPrice = booking.getFinalPrice() != null ? booking.getFinalPrice().longValue() : 0L;

        if (totalCollected == null || finalPrice <= 0 || totalCollected < finalPrice) {
            return;
        }

        int commissionRate = commissionService.getCommissionRateBps(booking.getTransportId());
        long platformFee = commissionService.calculatePlatformFee(finalPrice, booking.getTransportId());
        settlement.setCommissionRateBps(commissionRate);
        settlement.setPlatformFeeVnd(platformFee);
        settlement.setStatus(SettlementStatus.READY);
        settlement.setReadyAt(settlement.getReadyAt() == null ? LocalDateTime.now() : settlement.getReadyAt());
        settlement.setOnHoldReason(null);
        settlementRepository.save(settlement);

        creditTransportWalletIfNeeded(settlement);
    }

    private void creditTransportWalletIfNeeded(BookingSettlement settlement) {
        if (settlement == null || settlement.getTransportId() == null || settlement.getSettlementId() == null) {
            return;
        }
        if (settlement.getStatus() != SettlementStatus.READY) {
            return;
        }
        if (walletService.hasSettlementCredit(settlement.getSettlementId())) {
            return;
        }

        long netAmount = computeNetAmount(settlement);
        if (netAmount <= 0) {
            log.warn("Net amount {} for settlement {} is not positive. Wallet credit skipped.",
                    netAmount, settlement.getSettlementId());
            return;
        }

        TransportWallet wallet = walletService.getOrCreateWallet(settlement.getTransportId());
        walletService.creditWallet(wallet.getWalletId(), netAmount,
                WalletTransactionType.SETTLEMENT_CREDIT,
                WalletTransactionReferenceType.SETTLEMENT,
                settlement.getSettlementId(),
                "Settlement for booking " + settlement.getBookingId(),
                null);

        log.info("Credited transport wallet {} with {} VND for settlement {} (booking {}).",
                wallet.getWalletId(), netAmount, settlement.getSettlementId(), settlement.getBookingId());
    }

    private long computeNetAmount(BookingSettlement settlement) {
        if (settlement.getNetToTransportVnd() != null) {
            return settlement.getNetToTransportVnd();
        }
        long total = defaultZero(settlement.getTotalCollectedVnd());
        long gateway = defaultZero(settlement.getGatewayFeeVnd());
        long platform = defaultZero(settlement.getPlatformFeeVnd());
        long adjustment = defaultZero(settlement.getAdjustmentVnd());
        return commissionService.calculateNetToTransport(total, gateway, platform, adjustment);
    }

    private long defaultZero(Long value) {
        return value != null ? value : 0L;
    }

    private void updateBookingStatusAfterInitiation(Booking booking, BookingStatus targetStatus) {
        updateBookingStatusIfAdvanced(booking, targetStatus);
    }

    private void updateBookingStatusAfterPayment(Booking booking, PaymentType paymentType) {
        if (paymentType == PaymentType.DEPOSIT) {
            updateBookingStatusIfAdvanced(booking, BookingStatus.CONFIRMED);
        } else if (paymentType == PaymentType.REMAINING_PAYMENT || paymentType == PaymentType.TIP) {
            updateBookingStatusIfAdvanced(booking, BookingStatus.CONFIRMED_BY_CUSTOMER);
        }
    }

    private void updateBookingStatusIfAdvanced(Booking booking, BookingStatus newStatus) {
        if (newStatus == null) {
            return;
        }
        BookingStatus currentStatus = booking.getStatus();
        if (currentStatus == BookingStatus.CANCELLED) {
            return;
        }
        if (currentStatus.ordinal() >= newStatus.ordinal()) {
            return;
        }
        booking.setStatus(newStatus);
        bookingRepository.save(booking);
    }

    public List<PaymentResponseDTO> getPaymentHistory(Long bookingId, Long userId, UserRole userRole) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (userRole == UserRole.CUSTOMER && !booking.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("You can only view payments for your own bookings");
        }

        List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtAsc(bookingId);
        return payments.stream()
                .map(PaymentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Initiate deposit payment (30% of total).
     * Supports CASH and BANK_TRANSFER (no external payment gateways).
     */
    @Transactional
    public InitiateDepositResponse initiateDepositPayment(InitiateDepositRequest request, Long userId) {
        // 1. Validate booking exists and belongs to user
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        if (!booking.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("You can only make payments for your own bookings");
        }

        // 2. Check booking has final price (transport accepted)
        if (booking.getFinalPrice() == null) {
            throw new IllegalStateException("Booking does not have a final price yet. Please select a quotation first.");
        }

        // 3. Calculate deposit amount (30%)
        Double depositAmount = booking.getFinalPrice().doubleValue() * paymentConfig.getDepositPercentage();

        // 4. Check if deposit already paid
        List<Payment> existingPayments = paymentRepository.findByBookingId(request.getBookingId());
        boolean depositPaid = existingPayments.stream()
                .anyMatch(p -> PaymentType.DEPOSIT.equals(p.getPaymentType())
                && PaymentStatus.COMPLETED.equals(p.getStatus()));

        if (depositPaid) {
            return InitiateDepositResponse.builder()
                    .success(false)
                    .message("Deposit has already been paid for this booking")
                    .bookingId(request.getBookingId())
                    .build();
        }

        // 5. Create payment record
        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setPaymentType(PaymentType.DEPOSIT);
        payment.setPaymentMethod(mapPaymentMethod(request.getMethod()));
        payment.setAmount(BigDecimal.valueOf(depositAmount));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIdempotencyKey(UUID.randomUUID().toString());

        Payment savedPayment = paymentRepository.save(payment);
        updateBookingStatusAfterInitiation(booking, BookingStatus.CONFIRMED);

        // 6. Auto-complete payment if method is CASH
        if (PaymentMethod.CASH.equals(savedPayment.getPaymentMethod())) {
            Payment completedPayment = markPaymentAsCompleted(savedPayment, userId);
            handlePostCompletionSideEffects(completedPayment, booking);
            savedPayment = completedPayment;
        }

        // 7. No external payment gateway URL is needed for CASH or BANK_TRANSFER
        String paymentUrl = null;

        // 8. Return response
        return InitiateDepositResponse.builder()
                .success(true)
                .paymentId(savedPayment.getPaymentId().toString())
                .paymentUrl(paymentUrl)
                .bookingId(request.getBookingId())
                .depositAmount(depositAmount)
                .message("Payment initiated successfully")
                .build();
    }

    /**
     * Initiate remaining payment (70% of total + optional tip).
     * Supports CASH and BANK_TRANSFER (no external payment gateways).
     */
    @Transactional
    public InitiateRemainingPaymentResponse initiateRemainingPayment(InitiateRemainingPaymentRequest request, Long userId) {
        // 1. Validate booking exists and belongs to user
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        if (!booking.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("You can only make payments for your own bookings");
        }

        // 2. Check booking status is COMPLETED
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("Booking must be in COMPLETED status to pay remaining amount. Current status: " + booking.getStatus());
        }

        // 3. Check booking has final price
        if (booking.getFinalPrice() == null) {
            throw new IllegalStateException("Booking does not have a final price.");
        }

        // 4. Calculate remaining amount (70%)
        Double remainingPercentage = 1.0 - paymentConfig.getDepositPercentage();
        Long remainingAmountVnd = Math.round(booking.getFinalPrice().doubleValue() * remainingPercentage);
        Long tipAmountVnd = request.getTipAmountVnd() != null ? request.getTipAmountVnd() : 0L;
        Long totalAmountVnd = remainingAmountVnd + tipAmountVnd;

        // 5. Check if remaining payment already made
        List<Payment> existingPayments = paymentRepository.findByBookingId(request.getBookingId());
        boolean remainingPaid = existingPayments.stream()
                .anyMatch(p -> PaymentType.REMAINING_PAYMENT.equals(p.getPaymentType())
                && PaymentStatus.COMPLETED.equals(p.getStatus()));

        if (remainingPaid) {
            return InitiateRemainingPaymentResponse.builder()
                    .success(false)
                    .message("Remaining payment has already been paid for this booking")
                    .bookingId(request.getBookingId())
                    .build();
        }

        // 6. Create payment record
        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setPaymentType(PaymentType.REMAINING_PAYMENT);
        payment.setPaymentMethod(mapPaymentMethod(request.getMethod()));
        payment.setAmount(BigDecimal.valueOf(totalAmountVnd));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIdempotencyKey(UUID.randomUUID().toString());

        Payment savedPayment = paymentRepository.save(payment);
        Payment savedTip = null;
        String tipPaymentId = null;

        if (tipAmountVnd > 0) {
            Payment tipPayment = new Payment();
            tipPayment.setBookingId(request.getBookingId());
            tipPayment.setPaymentType(PaymentType.TIP);
            tipPayment.setPaymentMethod(mapPaymentMethod(request.getMethod()));
            tipPayment.setAmount(BigDecimal.valueOf(tipAmountVnd));
            tipPayment.setStatus(PaymentStatus.PENDING);
            tipPayment.setIdempotencyKey(UUID.randomUUID().toString());
            savedTip = paymentRepository.save(tipPayment);
            tipPaymentId = savedTip.getPaymentId().toString();
        }

        // 7. Auto-complete payment if method is CASH
        if (PaymentMethod.CASH.equals(savedPayment.getPaymentMethod())) {
            // Complete remaining payment
            Payment completedPayment = markPaymentAsCompleted(savedPayment, userId);
            handlePostCompletionSideEffects(completedPayment, booking);
            savedPayment = completedPayment;
            
            // Complete tip payment if exists
            if (savedTip != null) {
                Payment completedTip = markPaymentAsCompleted(savedTip, userId);
                handlePostCompletionSideEffects(completedTip, booking);
            }
        }

        // 8. No external payment gateway URL is needed for CASH or BANK_TRANSFER
        String paymentUrl = null;

        // Bank transfer doesn't need URL

        // 8. Update settlement if it exists
        Optional<BookingSettlement> settlementOpt = settlementRepository.findByBookingId(request.getBookingId());
        if (settlementOpt.isPresent()) {
            BookingSettlement settlement = settlementOpt.get();
            // Note: Settlement will be fully updated when payment is confirmed
            // For now, just log that settlement exists
            log.info("Settlement exists for booking {}. Will be updated when payment is confirmed.", request.getBookingId());
        }

        // 9. Return response
        return InitiateRemainingPaymentResponse.builder()
                .success(true)
                .paymentId(savedPayment.getPaymentId().toString())
                .tipPaymentId(tipPaymentId)
                .paymentUrl(paymentUrl)
                .bookingId(request.getBookingId())
                .remainingAmountVnd(remainingAmountVnd)
                .tipAmountVnd(tipAmountVnd)
                .totalAmountVnd(totalAmountVnd)
                .message("Remaining payment initiated successfully")
                .build();
    }


    /**
     * Get payment status for a booking
     */
    public PaymentStatusResponse getPaymentStatus(Long bookingId, String paymentId, Long userId) {
        // Validate booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!booking.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("Access denied");
        }

        // Find payment
        Payment payment;
        if (paymentId != null) {
            payment = paymentRepository.findById(Long.parseLong(paymentId))
                    .orElseThrow(() -> new PaymentNotFoundException(Long.parseLong(paymentId)));
        } else {
            // Get latest deposit payment
            List<Payment> payments = paymentRepository.findByBookingId(bookingId);
            payment = payments.stream()
                    .filter(p -> PaymentType.DEPOSIT.equals(p.getPaymentType()))
                    .findFirst()
                    .orElseThrow(() -> new PaymentNotFoundException("No deposit payment found for booking"));
        }

        // Map status
        String status = mapPaymentStatusForFrontend(payment.getStatus());

        return PaymentStatusResponse.builder()
                .success(true)
                .status(status)
                .amount(payment.getAmount().doubleValue())
                .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)
                .paymentDate(payment.getPaidAt() != null
                        ? payment.getPaidAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .message("Payment status retrieved successfully")
                .build();
    }

    /**
     * Get bank information for transfer.
     * Returns static bank details configured in PaymentConfig.
     */
    public BankInfoResponse getBankInfo(Long bookingId, Double amount) {
        PaymentConfig.BankInfo bankConfig = paymentConfig.getBank();

        return BankInfoResponse.builder()
                .bank(bankConfig.getBank())
                .accountNumber(bankConfig.getAccountNumber())
                .accountName(bankConfig.getAccountName())
                .branch(bankConfig.getBranch())
                .build();
    }

    /**
     * Get bank information without specific booking (generic)
     */
    public BankInfoResponse getBankInfoGeneric() {
        PaymentConfig.BankInfo bankConfig = paymentConfig.getBank();

        return BankInfoResponse.builder()
                .bank(bankConfig.getBank())
                .accountNumber(bankConfig.getAccountNumber())
                .accountName(bankConfig.getAccountName())
                .branch(bankConfig.getBranch())
                .build();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================
    
    /**
     * Mark payment as completed with timestamp and confirmedBy.
     * This is an internal helper - no permission checks here.
     */
    private Payment markPaymentAsCompleted(Payment payment, Long confirmedByUserId) {
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setConfirmedBy(confirmedByUserId);
            payment.setConfirmedAt(LocalDateTime.now());
            if (payment.getPaidAt() == null) {
                payment.setPaidAt(LocalDateTime.now());
            }
        }
        return paymentRepository.save(payment);
    }
    
    /**
     * Handle all post-completion side effects: settlement, wallet credit, booking status, notifications.
     * This is an internal helper - assumes payment is already COMPLETED.
     */
    private void handlePostCompletionSideEffects(Payment payment, Booking booking) {
        // Update settlement breakdown for customer payments
        if (PaymentType.DEPOSIT.equals(payment.getPaymentType())
                || PaymentType.REMAINING_PAYMENT.equals(payment.getPaymentType())
                || PaymentType.TIP.equals(payment.getPaymentType())) {
            BookingSettlement settlement = updateSettlementPaymentBreakdown(payment, booking);
            finalizeSettlementIfEligible(booking, settlement);
            if (PaymentType.REMAINING_PAYMENT.equals(payment.getPaymentType())) {
                creditTransportWalletIfNeeded(settlement);
            }
            updateBookingStatusAfterPayment(booking, payment.getPaymentType());
        }

        // Send notification about payment confirmation
        sendPaymentConfirmationNotification(payment);
    }
    
    private com.homeexpress.home_express_api.entity.PaymentMethod mapPaymentMethod(PaymentMethodRequest method) {
        if (method == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        return method.toEntityMethod();
    }

    private String mapPaymentStatusForFrontend(PaymentStatus status) {
        return switch (status) {
            case PENDING ->
                "PENDING";
            case PROCESSING ->
                "AWAITING_CUSTOMER";
            case COMPLETED ->
                "DEPOSIT_PAID";
            case FAILED ->
                "FAILED";
            case REFUNDED ->
                "CANCELLED";
        };
    }

}
