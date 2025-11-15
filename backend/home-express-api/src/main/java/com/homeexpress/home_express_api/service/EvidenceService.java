package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.incident.EvidenceRequest;
import com.homeexpress.home_express_api.dto.incident.EvidenceResponse;
import com.homeexpress.home_express_api.dto.request.UploadBookingEvidenceRequest;
import com.homeexpress.home_express_api.dto.response.BookingEvidenceResponse;
import com.homeexpress.home_express_api.entity.*;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.exception.UnauthorizedException;
import com.homeexpress.home_express_api.repository.BookingRepository;
import com.homeexpress.home_express_api.repository.EvidenceRepository;
import com.homeexpress.home_express_api.repository.IncidentRepository;
import com.homeexpress.home_express_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvidenceService {

    private static final Logger log = LoggerFactory.getLogger(EvidenceService.class);

    @Autowired
    private EvidenceRepository evidenceRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public EvidenceResponse uploadEvidence(EvidenceRequest request, Long userId, UserRole userRole) {
        Incident incident = incidentRepository.findById(request.getIncidentId())
            .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + request.getIncidentId()));

        Booking booking = bookingRepository.findById(incident.getBookingId())
            .orElseThrow(() -> new ResourceNotFoundException("Associated booking not found"));

        if (userRole == UserRole.CUSTOMER && !booking.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("You can only upload evidence for incidents in your own bookings");
        }

        if (userRole == UserRole.TRANSPORT && !userId.equals(booking.getTransportId())) {
            throw new UnauthorizedException("You can only upload evidence for incidents in bookings you are assigned to");
        }

        Evidence evidence = new Evidence();
        evidence.setIncidentId(request.getIncidentId());
        evidence.setUploadedByUserId(userId);
        evidence.setFileType(request.getFileType());
        evidence.setFileUrl(request.getFileUrl());
        evidence.setFileName(request.getFileName());
        evidence.setFileSizeBytes(request.getFileSizeBytes());
        evidence.setDescription(request.getDescription());

        Evidence savedEvidence = evidenceRepository.save(evidence);
        return EvidenceResponse.fromEntity(savedEvidence);
    }

    @Transactional(readOnly = true)
    public List<EvidenceResponse> getEvidenceByIncident(Long incidentId, Long userId, UserRole userRole) {
        Incident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + incidentId));

        if (userRole != UserRole.MANAGER) {
            Booking booking = bookingRepository.findById(incident.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated booking not found"));

            boolean isCustomer = userRole == UserRole.CUSTOMER && booking.getCustomerId().equals(userId);
            boolean isTransport = userRole == UserRole.TRANSPORT && userId.equals(booking.getTransportId());

            if (!isCustomer && !isTransport) {
                throw new UnauthorizedException("You are not authorized to view evidence for this incident");
            }
        }

        return evidenceRepository.findByIncidentIdOrderByUploadedAtDesc(incidentId)
            .stream()
            .map(EvidenceResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteEvidence(Long evidenceId, Long userId, UserRole userRole) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
            .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with ID: " + evidenceId));

        boolean isUploader = evidence.getUploadedByUserId().equals(userId);
        boolean isManager = userRole == UserRole.MANAGER;

        if (!isUploader && !isManager) {
            throw new UnauthorizedException("You can only delete evidence you uploaded or be a manager");
        }

        evidenceRepository.delete(evidence);
    }

    // ============================================================================
    // BOOKING EVIDENCE METHODS
    // ============================================================================

    /**
     * Upload evidence for a booking (not tied to an incident)
     */
    @Transactional
    public BookingEvidenceResponse uploadBookingEvidence(
            Long bookingId,
            UploadBookingEvidenceRequest request,
            Long userId,
            UserRole userRole) {

        // Validate booking exists
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // Validate authorization
        if (userRole == UserRole.CUSTOMER && !booking.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("You can only upload evidence for your own bookings");
        }

        if (userRole == UserRole.TRANSPORT && !userId.equals(booking.getTransportId())) {
            throw new UnauthorizedException("You can only upload evidence for bookings you are assigned to");
        }

        // Create evidence entity
        Evidence evidence = new Evidence();
        evidence.setBookingId(bookingId);
        evidence.setUploadedByUserId(userId);
        evidence.setEvidenceType(request.getEvidenceType());
        evidence.setFileType(request.getFileType());
        evidence.setFileUrl(request.getFileUrl());
        evidence.setFileName(request.getFileName());
        evidence.setMimeType(request.getMimeType());
        evidence.setFileSizeBytes(request.getFileSizeBytes());
        evidence.setDescription(request.getDescription());

        Evidence savedEvidence = evidenceRepository.save(evidence);

        log.info("Evidence uploaded for booking {}: evidenceId={}, type={}, uploadedBy={}",
                bookingId, savedEvidence.getEvidenceId(), request.getEvidenceType(), userId);

        return mapToBookingEvidenceResponse(savedEvidence);
    }

    /**
     * Get all evidence for a booking
     */
    @Transactional(readOnly = true)
    public List<BookingEvidenceResponse> getBookingEvidence(
            Long bookingId,
            Long userId,
            UserRole userRole,
            EvidenceType filterType) {

        // Validate booking exists
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // Validate authorization (customers can only view their own bookings)
        if (userRole == UserRole.CUSTOMER && !booking.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("You can only view evidence for your own bookings");
        }

        if (userRole == UserRole.TRANSPORT && !userId.equals(booking.getTransportId())) {
            throw new UnauthorizedException("You can only view evidence for bookings you are assigned to");
        }

        // Get evidence (with optional filter)
        List<Evidence> evidenceList;
        if (filterType != null) {
            evidenceList = evidenceRepository.findByBookingIdAndEvidenceTypeOrderByUploadedAtDesc(bookingId, filterType);
        } else {
            evidenceList = evidenceRepository.findByBookingIdOrderByUploadedAtDesc(bookingId);
        }

        return evidenceList.stream()
            .map(this::mapToBookingEvidenceResponse)
            .collect(Collectors.toList());
    }

    /**
     * Map Evidence entity to BookingEvidenceResponse with uploader details
     */
    private BookingEvidenceResponse mapToBookingEvidenceResponse(Evidence evidence) {
        BookingEvidenceResponse response = BookingEvidenceResponse.fromEntity(evidence);

        // Enrich with uploader details
        userRepository.findById(evidence.getUploadedByUserId()).ifPresent(user -> {
            response.setUploaderName(user.getFullName());
            response.setUploaderRole(user.getRole().name());
        });

        return response;
    }
}
