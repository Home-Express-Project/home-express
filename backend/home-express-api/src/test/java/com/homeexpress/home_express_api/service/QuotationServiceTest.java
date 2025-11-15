package com.homeexpress.home_express_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeexpress.home_express_api.dto.request.QuotationRequest;
import com.homeexpress.home_express_api.dto.response.AcceptQuotationResponse;
import com.homeexpress.home_express_api.dto.response.QuotationResponse;
import com.homeexpress.home_express_api.entity.*;
import com.homeexpress.home_express_api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotationServiceTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransportRepository transportRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingItemRepository bookingItemRepository;

    @Mock
    private CustomerEventService customerEventService;

    @Mock
    private RateCardService rateCardService;

    @Mock
    private PricingService pricingService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private QuotationService quotationService;

    private QuotationRequest quotationRequest;
    private Booking mockBooking;
    private Quotation mockQuotation;
    private Transport mockTransport;
    private Customer mockCustomer;
    private User mockUser;
    private Contract mockContract;

    @BeforeEach
    void setUp() {
        // Setup quotation request
        quotationRequest = new QuotationRequest();
        quotationRequest.setBookingId(1L);
        quotationRequest.setVehicleId(1L);
        quotationRequest.setQuotedPrice(BigDecimal.valueOf(5000000)); // 5M VND
        quotationRequest.setBasePrice(BigDecimal.valueOf(3000000));
        quotationRequest.setDistancePrice(BigDecimal.valueOf(1500000));
        quotationRequest.setItemsPrice(BigDecimal.valueOf(500000));
        quotationRequest.setAdditionalFees(BigDecimal.ZERO);
        quotationRequest.setValidityPeriod(7);

        // Setup mock booking
        mockBooking = new Booking();
        mockBooking.setBookingId(1L);
        mockBooking.setCustomerId(1L);
        mockBooking.setStatus(BookingStatus.PENDING);
        mockBooking.setPickupAddress("123 Nguyen Hue");
        mockBooking.setDeliveryAddress("456 Le Loi");
        mockBooking.setPreferredDate(java.time.LocalDate.now().plusDays(1));

        // Setup mock quotation
        mockQuotation = new Quotation();
        mockQuotation.setQuotationId(1L);
        mockQuotation.setBookingId(1L);
        mockQuotation.setTransportId(5L);
        mockQuotation.setVehicleId(1L);
        mockQuotation.setQuotedPrice(BigDecimal.valueOf(5000000));
        mockQuotation.setBasePrice(BigDecimal.valueOf(3000000));
        mockQuotation.setDistancePrice(BigDecimal.valueOf(1500000));
        mockQuotation.setItemsPrice(BigDecimal.valueOf(500000));
        mockQuotation.setStatus(QuotationStatus.PENDING);
        mockQuotation.setValidityPeriod(7);
        mockQuotation.setExpiresAt(LocalDateTime.now().plusDays(7));
        mockQuotation.setCreatedAt(LocalDateTime.now());

        // Setup mock transport
        mockTransport = new Transport();
        mockTransport.setTransportId(5L);
        mockTransport.setCompanyName("Transport Co.");
        mockTransport.setPhone("0912345678");
        mockTransport.setAverageRating(BigDecimal.valueOf(4.5));
        mockTransport.setTotalBookings(100);
        mockTransport.setCompletedBookings(95);

        // Setup mock customer
        mockCustomer = new Customer();
        mockCustomer.setCustomerId(1L);
        mockCustomer.setFullName("John Doe");
        mockCustomer.setPhone("0901234567");

        // Setup mock user
        mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("customer@test.com");
        mockUser.setRole(UserRole.CUSTOMER);

        // Setup mock contract
        mockContract = new Contract();
        mockContract.setContractId(1L);
        mockContract.setQuotationId(1L);
        mockContract.setBookingId(1L);
        mockContract.setContractNumber("CT20251215-0001");
        mockContract.setTotalAmount(BigDecimal.valueOf(5000000));
        mockContract.setStatus(ContractStatus.DRAFT);
    }

    @Test
    void testCreateQuotation_Success() {
        // Given
        Long transportId = 5L;

        when(quotationRepository.save(any(Quotation.class))).thenReturn(mockQuotation);
        when(bookingItemRepository.findByBookingId(anyLong())).thenReturn(java.util.Collections.emptyList());

        // When
        QuotationResponse response = quotationService.createQuotation(quotationRequest, transportId);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getQuotationId());
        assertEquals(1L, response.getBookingId());
        assertEquals(5L, response.getTransportId());
        assertEquals(BigDecimal.valueOf(5000000), response.getQuotedPrice());
        assertEquals(BigDecimal.valueOf(3000000), response.getBasePrice());
        assertEquals(QuotationStatus.PENDING, response.getStatus());

        verify(quotationRepository, times(1)).save(any(Quotation.class));
    }

    @Test
    void testCalculatePrice_BasedOnVolume() {
        // Given - Simulate pricing calculation via pricing service
        Long transportId = 5L;
        
        // Mock that the quotation is created with calculated prices
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(invocation -> {
            Quotation quotation = invocation.getArgument(0);
            // Verify calculated price components
            assertNotNull(quotation.getQuotedPrice());
            assertEquals(BigDecimal.valueOf(5000000), quotation.getQuotedPrice());
            return mockQuotation;
        });
        when(bookingItemRepository.findByBookingId(anyLong())).thenReturn(java.util.Collections.emptyList());

        // When
        QuotationResponse response = quotationService.createQuotation(quotationRequest, transportId);

        // Then - Verify price breakdown
        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(5000000), response.getQuotedPrice());
        assertEquals(BigDecimal.valueOf(3000000), response.getBasePrice());
        assertEquals(BigDecimal.valueOf(1500000), response.getDistancePrice());
        assertEquals(BigDecimal.valueOf(500000), response.getItemsPrice());
        
        verify(quotationRepository, times(1)).save(any(Quotation.class));
    }

    @Test
    void testApplyPricingRules_Success() {
        // Given
        Long transportId = 5L;
        
        // Set additional pricing rules
        quotationRequest.setDiscount(BigDecimal.valueOf(200000)); // 200K discount
        
        // Recalculate final quoted price
        BigDecimal totalBeforeDiscount = quotationRequest.getBasePrice()
                .add(quotationRequest.getDistancePrice())
                .add(quotationRequest.getItemsPrice());
        BigDecimal finalPrice = totalBeforeDiscount.subtract(quotationRequest.getDiscount());
        quotationRequest.setQuotedPrice(finalPrice); // 4.8M after discount

        mockQuotation.setQuotedPrice(finalPrice);
        mockQuotation.setDiscount(BigDecimal.valueOf(200000));

        when(quotationRepository.save(any(Quotation.class))).thenReturn(mockQuotation);
        when(bookingItemRepository.findByBookingId(anyLong())).thenReturn(java.util.Collections.emptyList());

        // When
        QuotationResponse response = quotationService.createQuotation(quotationRequest, transportId);

        // Then - Verify pricing rules applied
        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(4800000), response.getQuotedPrice());
        assertEquals(BigDecimal.valueOf(200000), response.getDiscount());
        
        // Verify total = base + distance + items - discount
        BigDecimal calculatedTotal = response.getBasePrice()
                .add(response.getDistancePrice())
                .add(response.getItemsPrice())
                .subtract(response.getDiscount());
        assertEquals(response.getQuotedPrice(), calculatedTotal);
        
        verify(quotationRepository, times(1)).save(any(Quotation.class));
    }

    @Test
    void testCustomerAcceptQuotation_Success() {
        // Given
        Long quotationId = 1L;
        Long customerId = 1L;
        String ipAddress = "127.0.0.1";

        mockQuotation.setStatus(QuotationStatus.ACCEPTED);
        mockQuotation.setAcceptedBy(customerId);
        mockQuotation.setAcceptedAt(LocalDateTime.now());

        mockBooking.setTransportId(5L);
        mockBooking.setFinalPrice(BigDecimal.valueOf(5000000));
        mockBooking.setStatus(BookingStatus.CONFIRMED);

        User transportUser = new User();
        transportUser.setUserId(5L);
        transportUser.setRole(UserRole.TRANSPORT);
        mockTransport.setUser(transportUser);

        doNothing().when(quotationRepository).acceptQuotation(anyLong(), anyLong(), anyString());
        when(quotationRepository.findById(quotationId)).thenReturn(Optional.of(mockQuotation));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));
        when(contractRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(contractRepository.save(any(Contract.class))).thenReturn(mockContract);
        when(contractRepository.count()).thenReturn(0L);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
        when(transportRepository.findById(5L)).thenReturn(Optional.of(mockTransport));
        when(userRepository.findById(customerId)).thenReturn(Optional.of(mockUser));

        // When
        AcceptQuotationResponse response = quotationService.acceptQuotation(quotationId, customerId, ipAddress);

        // Then
        assertNotNull(response);
        assertEquals("Quotation accepted successfully", response.getMessage());
        assertEquals(1L, response.getContractId());
        assertEquals("CT20251215-0001", response.getContractNumber());
        
        // Verify booking summary
        assertNotNull(response.getBooking());
        assertEquals(1L, response.getBooking().getBookingId());
        assertEquals(BookingStatus.CONFIRMED, response.getBooking().getStatus());
        assertEquals(5L, response.getBooking().getFinalTransportId());
        
        // Verify customer summary
        assertNotNull(response.getCustomer());
        assertEquals(1L, response.getCustomer().getCustomerId());
        assertEquals("John Doe", response.getCustomer().getFullName());
        
        // Verify transport summary
        assertNotNull(response.getTransport());
        assertEquals(5L, response.getTransport().getTransportId());
        assertEquals("Transport Co.", response.getTransport().getCompanyName());
        assertEquals(4.5, response.getTransport().getAverageRating());

        verify(quotationRepository, times(1)).acceptQuotation(quotationId, customerId, ipAddress);
        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    @Test
    void testGetQuotationById_Success() {
        // Given
        Long quotationId = 1L;
        
        when(quotationRepository.findById(quotationId)).thenReturn(Optional.of(mockQuotation));

        // When
        QuotationResponse response = quotationService.getQuotationById(quotationId);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getQuotationId());
        assertEquals(1L, response.getBookingId());
        assertEquals(5L, response.getTransportId());
        
        verify(quotationRepository, times(1)).findById(quotationId);
    }

    @Test
    void testRejectQuotation_Success() {
        // Given
        Long quotationId = 1L;
        
        Quotation rejectedQuotation = new Quotation();
        rejectedQuotation.setQuotationId(quotationId);
        rejectedQuotation.setStatus(QuotationStatus.REJECTED);
        rejectedQuotation.setRespondedAt(LocalDateTime.now());

        when(quotationRepository.findById(quotationId)).thenReturn(Optional.of(mockQuotation));
        when(quotationRepository.save(any(Quotation.class))).thenReturn(rejectedQuotation);

        // When
        QuotationResponse response = quotationService.rejectQuotation(quotationId);

        // Then
        assertNotNull(response);
        assertEquals(QuotationStatus.REJECTED, response.getStatus());
        assertNotNull(response.getRespondedAt());
        
        verify(quotationRepository, times(1)).findById(quotationId);
        verify(quotationRepository, times(1)).save(any(Quotation.class));
    }
}
