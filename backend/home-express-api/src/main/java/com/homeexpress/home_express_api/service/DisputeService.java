package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.request.AddDisputeMessageRequest;
import com.homeexpress.home_express_api.dto.request.CreateDisputeRequest;
import com.homeexpress.home_express_api.dto.request.UpdateDisputeStatusRequest;
import com.homeexpress.home_express_api.dto.response.DisputeMessageResponse;
import com.homeexpress.home_express_api.dto.response.DisputeResponse;
import com.homeexpress.home_express_api.entity.*;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.exception.UnauthorizedException;
import com.homeexpress.home_express_api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing disputes and dispute messages.
 * Handles dispute creation, status updates, message threads, and evidence attachment.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final DisputeMessageRepository disputeMessageRepository;
    private final BookingRepository bookingRepository;
    private final EvidenceRepository evidenceRepository;
    private final UserRepository userRepository;
    private final CustomerEventService customerEventService;

    /**
     * Create a new dispute for a booking
     */
    @Transactional
    public DisputeResponse createDispute(Long bookingId, CreateDisputeRequest request, 
                                         Long userId, UserRole userRole) {
        log.info("Creating dispute for booking {} by user {} (role: {})", bookingId, userId, userRole);

        // Validate booking exists
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // Validate user has access to this booking
        validateBookingAccess(booking, userId, userRole);

        // Create dispute entity
        Dispute dispute = Dispute.builder()
                .bookingId(bookingId)
                .filedByUserId(userId)
                .disputeType(request.getDisputeType())
                .status(DisputeStatus.PENDING)
                .title(request.getTitle())
                .description(request.getDescription())
                .requestedResolution(request.getRequestedResolution())
                .build();

        // Attach evidence if provided
        if (request.getEvidenceIds() != null && !request.getEvidenceIds().isEmpty()) {
            for (Long evidenceId : request.getEvidenceIds()) {
                Evidence evidence = evidenceRepository.findById(evidenceId)
                        .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with ID: " + evidenceId));
                
                // Validate evidence belongs to this booking
                if (!bookingId.equals(evidence.getBookingId())) {
                    throw new IllegalArgumentException("Evidence " + evidenceId + " does not belong to booking " + bookingId);
                }
                
                dispute.addEvidence(evidence);
            }
        }

        dispute = disputeRepository.save(dispute);
        log.info("Created dispute {} for booking {}", dispute.getDisputeId(), bookingId);

        // Send SSE event
        DisputeResponse response = mapToDisputeResponse(dispute);
        sendDisputeEvent(booking.getCustomerId(), response, "dispute_created");

        return response;
    }

    /**
     * Get all disputes for a booking
     */
    @Transactional(readOnly = true)
    public List<DisputeResponse> getBookingDisputes(Long bookingId, Long userId, UserRole userRole) {
        log.debug("Getting disputes for booking {} by user {} (role: {})", bookingId, userId, userRole);

        // Validate booking exists and user has access
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        
        validateBookingAccess(booking, userId, userRole);

        List<Dispute> disputes = disputeRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
        return disputes.stream()
                .map(this::mapToDisputeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a single dispute by ID
     */
    @Transactional(readOnly = true)
    public DisputeResponse getDisputeById(Long disputeId, Long userId, UserRole userRole) {
        log.debug("Getting dispute {} by user {} (role: {})", disputeId, userId, userRole);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found with ID: " + disputeId));

        // Validate user has access to this dispute's booking
        Booking booking = bookingRepository.findById(dispute.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + dispute.getBookingId()));
        
        validateBookingAccess(booking, userId, userRole);

        return mapToDisputeResponse(dispute);
    }

    /**
     * Add a message to a dispute thread
     */
    @Transactional
    public DisputeMessageResponse addDisputeMessage(Long disputeId, AddDisputeMessageRequest request, 
                                                     Long userId, UserRole userRole) {
        log.info("Adding message to dispute {} by user {} (role: {})", disputeId, userId, userRole);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found with ID: " + disputeId));

        // Validate user has access to this dispute's booking
        Booking booking = bookingRepository.findById(dispute.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + dispute.getBookingId()));
        
        validateBookingAccess(booking, userId, userRole);

        // Create message
        DisputeMessage message = DisputeMessage.builder()
                .disputeId(disputeId)
                .senderUserId(userId)
                .messageText(request.getMessageText())
                .build();

        message = disputeMessageRepository.save(message);
        dispute.addMessage(message);
        
        log.info("Added message {} to dispute {}", message.getMessageId(), disputeId);

        // Send SSE event
        DisputeResponse disputeResponse = mapToDisputeResponse(dispute);
        sendDisputeEvent(booking.getCustomerId(), disputeResponse, "dispute_message_added");

        return mapToDisputeMessageResponse(message);
    }

    /**
     * Get all messages for a dispute
     */
    @Transactional(readOnly = true)
    public List<DisputeMessageResponse> getDisputeMessages(Long disputeId, Long userId, UserRole userRole) {
        log.debug("Getting messages for dispute {} by user {} (role: {})", disputeId, userId, userRole);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found with ID: " + disputeId));

        // Validate user has access to this dispute's booking
        Booking booking = bookingRepository.findById(dispute.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + dispute.getBookingId()));
        
        validateBookingAccess(booking, userId, userRole);

        List<DisputeMessage> messages = disputeMessageRepository.findByDisputeIdOrderByCreatedAtAsc(disputeId);
        return messages.stream()
                .map(this::mapToDisputeMessageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update dispute status (MANAGER only)
     */
    @Transactional
    public DisputeResponse updateDisputeStatus(Long disputeId, UpdateDisputeStatusRequest request, 
                                                Long userId, UserRole userRole) {
        log.info("Updating dispute {} status to {} by user {} (role: {})", 
                disputeId, request.getStatus(), userId, userRole);

        // Only managers can update dispute status
        if (userRole != UserRole.MANAGER) {
            throw new UnauthorizedException("Only managers can update dispute status");
        }

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found with ID: " + disputeId));

        // Update status based on request
        DisputeStatus newStatus = request.getStatus();
        if (newStatus == DisputeStatus.RESOLVED) {
            dispute.resolve(userId, request.getResolutionNotes());
        } else if (newStatus == DisputeStatus.REJECTED) {
            dispute.reject(userId, request.getResolutionNotes());
        } else if (newStatus == DisputeStatus.ESCALATED) {
            dispute.escalate();
        } else if (newStatus == DisputeStatus.UNDER_REVIEW) {
            dispute.putUnderReview();
        } else {
            dispute.setStatus(newStatus);
        }

        dispute = disputeRepository.save(dispute);
        log.info("Updated dispute {} status to {}", disputeId, newStatus);

        // Send SSE event
        Booking booking = bookingRepository.findById(dispute.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        DisputeResponse response = mapToDisputeResponse(dispute);
        sendDisputeEvent(booking.getCustomerId(), response, "dispute_status_changed");

        return response;
    }

    /**
     * Attach evidence to a dispute
     */
    @Transactional
    public void attachEvidence(Long disputeId, Long evidenceId, Long userId, UserRole userRole) {
        log.info("Attaching evidence {} to dispute {} by user {} (role: {})", 
                evidenceId, disputeId, userId, userRole);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found with ID: " + disputeId));

        // Validate user has access to this dispute's booking
        Booking booking = bookingRepository.findById(dispute.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + dispute.getBookingId()));
        
        validateBookingAccess(booking, userId, userRole);

        // Validate evidence exists and belongs to the same booking
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with ID: " + evidenceId));
        
        if (!dispute.getBookingId().equals(evidence.getBookingId())) {
            throw new IllegalArgumentException("Evidence does not belong to the same booking as the dispute");
        }

        dispute.addEvidence(evidence);
        disputeRepository.save(dispute);
        
        log.info("Attached evidence {} to dispute {}", evidenceId, disputeId);
    }

    /**
     * Map Dispute entity to DisputeResponse DTO
     */
    private DisputeResponse mapToDisputeResponse(Dispute dispute) {
        // Get filed by user info
        User filedByUser = userRepository.findById(dispute.getFiledByUserId()).orElse(null);
        String filedByUserName = filedByUser != null ? getUserDisplayName(filedByUser) : "Unknown";
        UserRole filedByUserRole = filedByUser != null ? filedByUser.getRole() : null;

        // Get resolved by user info if applicable
        String resolvedByUserName = null;
        if (dispute.getResolvedByUserId() != null) {
            User resolvedByUser = userRepository.findById(dispute.getResolvedByUserId()).orElse(null);
            resolvedByUserName = resolvedByUser != null ? getUserDisplayName(resolvedByUser) : "Unknown";
        }

        // Get booking status
        Booking booking = bookingRepository.findById(dispute.getBookingId()).orElse(null);
        String bookingStatus = booking != null ? booking.getStatus().name() : null;

        // Get counts
        Long messageCount = disputeMessageRepository.countByDisputeId(dispute.getDisputeId());
        Long evidenceCount = (long) dispute.getEvidence().size();

        return DisputeResponse.builder()
                .disputeId(dispute.getDisputeId())
                .bookingId(dispute.getBookingId())
                .bookingStatus(bookingStatus)
                .filedByUserId(dispute.getFiledByUserId())
                .filedByUserName(filedByUserName)
                .filedByUserRole(filedByUserRole)
                .disputeType(dispute.getDisputeType())
                .status(dispute.getStatus())
                .title(dispute.getTitle())
                .description(dispute.getDescription())
                .requestedResolution(dispute.getRequestedResolution())
                .resolutionNotes(dispute.getResolutionNotes())
                .resolvedByUserId(dispute.getResolvedByUserId())
                .resolvedByUserName(resolvedByUserName)
                .resolvedAt(dispute.getResolvedAt())
                .messageCount(messageCount)
                .evidenceCount(evidenceCount)
                .createdAt(dispute.getCreatedAt())
                .updatedAt(dispute.getUpdatedAt())
                .build();
    }

    /**
     * Map DisputeMessage entity to DisputeMessageResponse DTO
     */
    private DisputeMessageResponse mapToDisputeMessageResponse(DisputeMessage message) {
        User sender = userRepository.findById(message.getSenderUserId()).orElse(null);
        String senderName = sender != null ? getUserDisplayName(sender) : "Unknown";
        UserRole senderRole = sender != null ? sender.getRole() : null;

        return DisputeMessageResponse.builder()
                .messageId(message.getMessageId())
                .disputeId(message.getDisputeId())
                .senderUserId(message.getSenderUserId())
                .senderName(senderName)
                .senderRole(senderRole)
                .messageText(message.getMessageText())
                .createdAt(message.getCreatedAt())
                .build();
    }

    /**
     * Get display name for a user based on their role
     */
    private String getUserDisplayName(User user) {
        // This is a simplified version - you may want to fetch from Customer/Transport/Manager tables
        return user.getEmail(); // Fallback to email
    }

    /**
     * Validate that a user has access to a booking
     */
    private void validateBookingAccess(Booking booking, Long userId, UserRole userRole) {
        if (userRole == UserRole.MANAGER) {
            // Managers can access all bookings
            return;
        }

        if (userRole == UserRole.CUSTOMER) {
            // Customers can only access their own bookings
            if (!booking.getCustomerId().equals(userId)) {
                throw new UnauthorizedException("You do not have access to this booking");
            }
        } else if (userRole == UserRole.TRANSPORT) {
            // Transport providers can access bookings assigned to them
            if (booking.getTransportId() == null || !booking.getTransportId().equals(userId)) {
                throw new UnauthorizedException("You do not have access to this booking");
            }
        } else {
            throw new UnauthorizedException("Invalid user role");
        }
    }

    /**
     * Send SSE event for dispute updates
     */
    private void sendDisputeEvent(Long customerId, DisputeResponse dispute, String eventType) {
        try {
            customerEventService.sendDisputeUpdate(customerId, dispute, eventType);
        } catch (Exception e) {
            log.error("Failed to send SSE event for dispute {}: {}", dispute.getDisputeId(), e.getMessage());
        }
    }
}

