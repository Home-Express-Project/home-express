package com.homeexpress.home_express_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.homeexpress.home_express_api.dto.SettlementDTO;
import com.homeexpress.home_express_api.dto.SettlementEligibilityDTO;
import com.homeexpress.home_express_api.entity.Booking;
import com.homeexpress.home_express_api.entity.BookingSettlement;
import com.homeexpress.home_express_api.entity.BookingStatus;
import com.homeexpress.home_express_api.entity.CollectionMode;
import com.homeexpress.home_express_api.entity.Contract;
import com.homeexpress.home_express_api.entity.Incident;
import com.homeexpress.home_express_api.entity.IncidentStatus;
import com.homeexpress.home_express_api.entity.Payment;
import com.homeexpress.home_express_api.entity.PaymentMethod;
import com.homeexpress.home_express_api.entity.PaymentStatus;
import com.homeexpress.home_express_api.entity.PaymentType;
import com.homeexpress.home_express_api.entity.SettlementStatus;
import com.homeexpress.home_express_api.entity.TransportWallet;
import com.homeexpress.home_express_api.repository.BookingRepository;
import com.homeexpress.home_express_api.repository.BookingSettlementRepository;
import com.homeexpress.home_express_api.repository.ContractRepository;
import com.homeexpress.home_express_api.repository.IncidentRepository;
import com.homeexpress.home_express_api.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private BookingSettlementRepository settlementRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private CommissionService commissionService;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private SettlementService settlementService;

    private Booking booking;
    private Contract contract;
    private List<Payment> payments;
    private BookingSettlement settlement;
    private TransportWallet wallet;

    @BeforeEach
    void setUp() {
        booking = new Booking();
        booking.setBookingId(1L);
        booking.setTransportId(100L);
        booking.setStatus(BookingStatus.COMPLETED);

        contract = new Contract();
        contract.setContractId(10L);
        contract.setBookingId(1L);
        contract.setAgreedPriceVnd(5000000L);

        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setBookingId(1L);
        payment.setAmount(BigDecimal.valueOf(5000000L));
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setPaymentType(PaymentType.DEPOSIT);

        payments = new ArrayList<>();
        payments.add(payment);

        settlement = new BookingSettlement();
        settlement.setSettlementId(1L);
        settlement.setBookingId(1L);
        settlement.setTransportId(100L);
        settlement.setAgreedPriceVnd(5000000L);
        settlement.setTotalCollectedVnd(5000000L);
        settlement.setGatewayFeeVnd(0L);
        settlement.setCommissionRateBps(1500);
        settlement.setPlatformFeeVnd(750000L);
        settlement.setAdjustmentVnd(0L);
        settlement.setCollectionMode(CollectionMode.ALL_CASH);
        settlement.setStatus(SettlementStatus.READY);
        settlement.setReadyAt(LocalDateTime.now());

        wallet = new TransportWallet();
        wallet.setWalletId(1L);
        wallet.setTransportId(100L);
        wallet.setCurrentBalanceVnd(4250000L);
    }

    @Test
    void generateSettlement_Success() {
        when(settlementRepository.existsByBookingId(1L)).thenReturn(false);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(contractRepository.findByBookingId(1L)).thenReturn(Optional.of(contract));
        when(paymentRepository.findByBookingIdOrderByCreatedAtAsc(1L)).thenReturn(payments);
        when(paymentRepository.sumAmountByBookingIdAndStatus(1L, PaymentStatus.COMPLETED))
                .thenReturn(BigDecimal.valueOf(5000000L));
        when(incidentRepository.findByBookingIdAndStatusOrderByReportedAtDesc(1L, IncidentStatus.REPORTED))
                .thenReturn(new ArrayList<>());
        when(incidentRepository.findByBookingIdAndStatusOrderByReportedAtDesc(1L, IncidentStatus.UNDER_INVESTIGATION))
                .thenReturn(new ArrayList<>());
        when(commissionService.getCommissionRateBps(100L)).thenReturn(1500);
        when(commissionService.calculatePlatformFee(5000000L, 100L)).thenReturn(750000L);
        when(commissionService.calculateNetToTransport(5000000L, 0L, 750000L, 0L)).thenReturn(4250000L);
        when(settlementRepository.save(any(BookingSettlement.class))).thenReturn(settlement);
        when(walletService.hasSettlementCredit(any())).thenReturn(false);
        when(walletService.getOrCreateWallet(100L)).thenReturn(wallet);

        SettlementDTO result = settlementService.generateSettlement(1L);

        assertNotNull(result);
        assertEquals(1L, result.getSettlementId());
        assertEquals(1L, result.getBookingId());
        assertEquals(100L, result.getTransportId());
        assertEquals(5000000L, result.getAgreedPriceVnd());
        assertEquals(5000000L, result.getTotalCollectedVnd());
        assertEquals(SettlementStatus.READY, result.getStatus());
        verify(settlementRepository).save(any(BookingSettlement.class));
        verify(walletService).creditWallet(anyLong(), anyLong(), any(), any(), any(), anyString(), any());
    }

    @Test
    void checkEligibilityForSettlement_EligibleBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(contractRepository.findByBookingId(1L)).thenReturn(Optional.of(contract));
        when(paymentRepository.sumAmountByBookingIdAndStatus(1L, PaymentStatus.COMPLETED))
                .thenReturn(BigDecimal.valueOf(5000000L));
        when(incidentRepository.findByBookingIdAndStatusOrderByReportedAtDesc(1L, IncidentStatus.REPORTED))
                .thenReturn(new ArrayList<>());
        when(incidentRepository.findByBookingIdAndStatusOrderByReportedAtDesc(1L, IncidentStatus.UNDER_INVESTIGATION))
                .thenReturn(new ArrayList<>());

        SettlementEligibilityDTO result = settlementService.checkEligibilityForSettlement(1L);

        assertTrue(result.isEligible());
        assertEquals(1L, result.getBookingId());
        assertEquals("COMPLETED", result.getBookingStatus());
        assertEquals(5000000L, result.getAgreedPriceVnd());
        assertEquals(5000000L, result.getTotalCollectedVnd());
        assertTrue(result.isFullyPaid());
        assertEquals(0, result.getOpenIncidentCount());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void calculateSettlementAmounts_Success() {
        when(commissionService.getCommissionRateBps(100L)).thenReturn(1500);
        when(commissionService.calculatePlatformFee(5000000L, 100L)).thenReturn(750000L);
        when(commissionService.calculateNetToTransport(5000000L, 0L, 750000L, 0L)).thenReturn(4250000L);

        SettlementService.SettlementAmounts result = settlementService.calculateSettlementAmounts(
                booking, contract, payments);

        assertNotNull(result);
        assertEquals(5000000L, result.agreedPriceVnd);
        assertEquals(5000000L, result.totalCollectedVnd);
        assertEquals(0L, result.gatewayFeeVnd);
        assertEquals(1500, result.commissionRateBps);
        assertEquals(750000L, result.platformFeeVnd);
        assertEquals(4250000L, result.netToTransportVnd);
    }
}
