package com.homeexpress.home_express_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

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

import com.homeexpress.home_express_api.dto.payout.PayoutDTO;
import com.homeexpress.home_express_api.entity.BookingSettlement;
import com.homeexpress.home_express_api.entity.PayoutStatus;
import com.homeexpress.home_express_api.entity.SettlementStatus;
import com.homeexpress.home_express_api.entity.Transport;
import com.homeexpress.home_express_api.entity.TransportPayout;
import com.homeexpress.home_express_api.entity.TransportPayoutItem;
import com.homeexpress.home_express_api.entity.TransportWallet;
import com.homeexpress.home_express_api.integration.payout.ExternalPayoutGateway;
import com.homeexpress.home_express_api.repository.BookingSettlementRepository;
import com.homeexpress.home_express_api.repository.TransportPayoutItemRepository;
import com.homeexpress.home_express_api.repository.TransportPayoutRepository;
import com.homeexpress.home_express_api.repository.TransportRepository;

@ExtendWith(MockitoExtension.class)
class PayoutServiceTest {

    @Mock
    private TransportPayoutRepository payoutRepository;

    @Mock
    private TransportPayoutItemRepository payoutItemRepository;

    @Mock
    private BookingSettlementRepository settlementRepository;

    @Mock
    private TransportRepository transportRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private ExternalPayoutGateway externalPayoutGateway;

    @InjectMocks
    private PayoutService payoutService;

    private Transport transport;
    private List<BookingSettlement> readySettlements;
    private TransportPayout payout;
    private TransportWallet wallet;
    private List<TransportPayoutItem> payoutItems;

    @BeforeEach
    void setUp() {
        transport = new Transport();
        transport.setTransportId(100L);
        transport.setCompanyName("Test Transport");
        transport.setBankCode("VIETCOMBANK");
        transport.setBankAccountNumber("1234567890");
        transport.setBankAccountHolder("Test Company");

        BookingSettlement settlement1 = mock(BookingSettlement.class);
        lenient().when(settlement1.getSettlementId()).thenReturn(1L);
        lenient().when(settlement1.getBookingId()).thenReturn(10L);
        lenient().when(settlement1.getTransportId()).thenReturn(100L);
        lenient().when(settlement1.getNetToTransportVnd()).thenReturn(2000000L);
        lenient().when(settlement1.getStatus()).thenReturn(SettlementStatus.READY);
        lenient().when(settlement1.getPayoutId()).thenReturn(null);

        BookingSettlement settlement2 = mock(BookingSettlement.class);
        lenient().when(settlement2.getSettlementId()).thenReturn(2L);
        lenient().when(settlement2.getBookingId()).thenReturn(11L);
        lenient().when(settlement2.getTransportId()).thenReturn(100L);
        lenient().when(settlement2.getNetToTransportVnd()).thenReturn(3000000L);
        lenient().when(settlement2.getStatus()).thenReturn(SettlementStatus.READY);
        lenient().when(settlement2.getPayoutId()).thenReturn(null);

        readySettlements = new ArrayList<>();
        readySettlements.add(settlement1);
        readySettlements.add(settlement2);

        payout = new TransportPayout();
        payout.setPayoutId(1L);
        payout.setTransportId(100L);
        payout.setPayoutNumber("PO-100-20240101120000");
        payout.setTotalAmountVnd(5000000L);
        payout.setItemCount(2);
        payout.setStatus(PayoutStatus.PENDING);
        payout.setBankCode("VIETCOMBANK");
        payout.setBankAccountNumber("1234567890");
        payout.setBankAccountHolder("Test Company");
        payout.setCreatedAt(LocalDateTime.now());

        wallet = new TransportWallet();
        wallet.setWalletId(1L);
        wallet.setTransportId(100L);
        wallet.setCurrentBalanceVnd(5000000L);

        TransportPayoutItem item1 = new TransportPayoutItem();
        item1.setPayoutItemId(1L);
        item1.setPayoutId(1L);
        item1.setSettlementId(1L);
        item1.setBookingId(10L);
        item1.setAmountVnd(2000000L);

        TransportPayoutItem item2 = new TransportPayoutItem();
        item2.setPayoutItemId(2L);
        item2.setPayoutId(1L);
        item2.setSettlementId(2L);
        item2.setBookingId(11L);
        item2.setAmountVnd(3000000L);

        payoutItems = new ArrayList<>();
        payoutItems.add(item1);
        payoutItems.add(item2);
    }

    @Test
    void createPayoutBatch_Success() {
        when(transportRepository.findById(100L)).thenReturn(Optional.of(transport));
        when(settlementRepository.findByTransportIdAndStatus(100L, SettlementStatus.READY))
                .thenReturn(readySettlements);
        when(walletService.getOrCreateWallet(100L)).thenReturn(wallet);
        when(payoutRepository.save(any(TransportPayout.class))).thenReturn(payout);
        when(payoutItemRepository.saveAll(anyList())).thenReturn(payoutItems);
        when(settlementRepository.saveAll(anyList())).thenReturn(readySettlements);

        PayoutDTO result = payoutService.createPayoutBatch(100L);

        assertNotNull(result);
        assertEquals(1L, result.getPayoutId());
        assertEquals(100L, result.getTransportId());
        assertEquals(5000000L, result.getTotalAmountVnd());
        assertEquals(2, result.getItemCount());
        assertEquals(PayoutStatus.PENDING, result.getStatus());
        assertEquals("VIETCOMBANK", result.getBankCode());
        assertNotNull(result.getItems());
        assertEquals(2, result.getItems().size());
        verify(payoutRepository).save(any(TransportPayout.class));
        verify(payoutItemRepository).saveAll(anyList());
        verify(settlementRepository).saveAll(anyList());
    }

    @Test
    void updatePayoutStatus_ToCompleted() {
        payout.setStatus(PayoutStatus.PROCESSING);
        when(payoutRepository.findById(1L)).thenReturn(Optional.of(payout));
        when(walletService.hasReferenceTransaction(any(), anyLong(), any())).thenReturn(false);
        when(walletService.getOrCreateWallet(100L)).thenReturn(wallet);
        when(payoutItemRepository.findByPayoutId(1L)).thenReturn(payoutItems);
        when(settlementRepository.findById(anyLong())).thenReturn(Optional.of(readySettlements.get(0)));
        when(payoutRepository.save(any(TransportPayout.class))).thenAnswer(invocation -> {
            TransportPayout saved = invocation.getArgument(0);
            saved.setCompletedAt(LocalDateTime.now());
            return saved;
        });

        PayoutDTO result = payoutService.updatePayoutStatus(1L, PayoutStatus.COMPLETED, null, "TXN123456");

        assertNotNull(result);
        assertEquals(PayoutStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedAt());
        assertEquals("TXN123456", result.getTransactionReference());
        verify(payoutRepository).save(any(TransportPayout.class));
        verify(walletService).debitWallet(anyLong(), anyLong(), any(), any(), any(), anyString(), any());
    }

    @Test
    void getPayoutDetails_Success() {
        when(payoutRepository.findById(1L)).thenReturn(Optional.of(payout));
        when(payoutItemRepository.findByPayoutId(1L)).thenReturn(payoutItems);

        PayoutDTO result = payoutService.getPayoutDetails(1L);

        assertNotNull(result);
        assertEquals(1L, result.getPayoutId());
        assertEquals(100L, result.getTransportId());
        assertEquals(5000000L, result.getTotalAmountVnd());
        assertNotNull(result.getItems());
        assertEquals(2, result.getItems().size());
        assertEquals(2000000L, result.getItems().get(0).getAmountVnd());
        assertEquals(3000000L, result.getItems().get(1).getAmountVnd());
        verify(payoutRepository).findById(1L);
        verify(payoutItemRepository).findByPayoutId(1L);
    }
}
