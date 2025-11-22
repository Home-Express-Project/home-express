package com.homeexpress.home_express_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.homeexpress.home_express_api.dto.request.AddDisputeMessageRequest;
import com.homeexpress.home_express_api.dto.request.CreateDisputeRequest;
import com.homeexpress.home_express_api.dto.request.UpdateDisputeStatusRequest;
import com.homeexpress.home_express_api.dto.response.DisputeMessageResponse;
import com.homeexpress.home_express_api.dto.response.DisputeResponse;
import com.homeexpress.home_express_api.entity.Booking;
import com.homeexpress.home_express_api.entity.BookingStatus;
import com.homeexpress.home_express_api.entity.Dispute;
import com.homeexpress.home_express_api.entity.DisputeMessage;
import com.homeexpress.home_express_api.entity.DisputeStatus;
import com.homeexpress.home_express_api.entity.DisputeType;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.repository.BookingRepository;
import com.homeexpress.home_express_api.repository.DisputeMessageRepository;
import com.homeexpress.home_express_api.repository.DisputeRepository;
import com.homeexpress.home_express_api.repository.EvidenceRepository;
import com.homeexpress.home_express_api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private DisputeMessageRepository disputeMessageRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerEventService customerEventService;

    @InjectMocks
    private DisputeService disputeService;

    private Booking booking;
    private User customerUser;
    private Dispute dispute;
    private CreateDisputeRequest createRequest;

    @BeforeEach
    void setUp() {
        customerUser = new User();
        customerUser.setUserId(1L);
        customerUser.setEmail("customer@test.com");
        customerUser.setRole(UserRole.CUSTOMER);

        booking = new Booking();
        booking.setBookingId(100L);
        booking.setCustomerId(1L);
        booking.setTransportId(200L);
        booking.setStatus(BookingStatus.COMPLETED);

        dispute = Dispute.builder()
                .disputeId(10L)
                .bookingId(100L)
                .filedByUserId(1L)
                .disputeType(DisputeType.PRICING_DISPUTE)
                .status(DisputeStatus.PENDING)
                .title("Overcharge issue")
                .description("I was charged more than the agreed price")
                .requestedResolution("Refund the difference")
                .build();

        createRequest = new CreateDisputeRequest();
        createRequest.setDisputeType(DisputeType.PRICING_DISPUTE);
        createRequest.setTitle("Overcharge issue");
        createRequest.setDescription("I was charged more than the agreed price");
        createRequest.setRequestedResolution("Refund the difference");
    }

    @Test
    void createDispute_Success() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(dispute);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
        when(disputeMessageRepository.countByDisputeId(10L)).thenReturn(0L);
        doNothing().when(customerEventService).sendDisputeUpdate(anyLong(), any(), anyString());

        DisputeResponse result = disputeService.createDispute(100L, createRequest, 1L, UserRole.CUSTOMER);

        assertNotNull(result);
        assertEquals(10L, result.getDisputeId());
        assertEquals(100L, result.getBookingId());
        assertEquals(DisputeType.PRICING_DISPUTE, result.getDisputeType());
        assertEquals(DisputeStatus.PENDING, result.getStatus());
        assertEquals("Overcharge issue", result.getTitle());
        verify(bookingRepository, atLeastOnce()).findById(100L);
        verify(disputeRepository).save(any(Dispute.class));
    }

    @Test
    void addDisputeMessage_Success() {
        DisputeMessage message = DisputeMessage.builder()
                .messageId(1L)
                .disputeId(10L)
                .senderUserId(1L)
                .messageText("Please review my case")
                .build();

        AddDisputeMessageRequest messageRequest = new AddDisputeMessageRequest();
        messageRequest.setMessageText("Please review my case");

        when(disputeRepository.findById(10L)).thenReturn(Optional.of(dispute));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(disputeMessageRepository.save(any(DisputeMessage.class))).thenReturn(message);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
        when(disputeMessageRepository.countByDisputeId(10L)).thenReturn(1L);
        doNothing().when(customerEventService).sendDisputeUpdate(anyLong(), any(), anyString());

        DisputeMessageResponse result = disputeService.addDisputeMessage(10L, messageRequest, 1L, UserRole.CUSTOMER);

        assertNotNull(result);
        assertEquals(1L, result.getMessageId());
        assertEquals(10L, result.getDisputeId());
        assertEquals(1L, result.getSenderUserId());
        assertEquals("Please review my case", result.getMessageText());
        verify(disputeMessageRepository).save(any(DisputeMessage.class));
    }

    @Test
    void updateDisputeStatus_Success() {
        User managerUser = new User();
        managerUser.setUserId(5L);
        managerUser.setEmail("manager@test.com");
        managerUser.setRole(UserRole.MANAGER);

        UpdateDisputeStatusRequest updateRequest = new UpdateDisputeStatusRequest();
        updateRequest.setStatus(DisputeStatus.RESOLVED);
        updateRequest.setResolutionNotes("Issue resolved with partial refund");

        when(disputeRepository.findById(10L)).thenReturn(Optional.of(dispute));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(dispute);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
        when(userRepository.findById(5L)).thenReturn(Optional.of(managerUser));
        when(disputeMessageRepository.countByDisputeId(10L)).thenReturn(0L);
        doNothing().when(customerEventService).sendDisputeUpdate(anyLong(), any(), anyString());

        DisputeResponse result = disputeService.updateDisputeStatus(10L, updateRequest, 5L, UserRole.MANAGER);

        assertNotNull(result);
        assertEquals(10L, result.getDisputeId());
        verify(disputeRepository).save(any(Dispute.class));
    }
}
