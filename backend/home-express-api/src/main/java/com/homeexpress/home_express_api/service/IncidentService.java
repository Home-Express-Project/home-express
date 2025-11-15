package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.incident.IncidentRequest;
import com.homeexpress.home_express_api.dto.incident.IncidentResponse;
import com.homeexpress.home_express_api.dto.incident.IncidentUpdateRequest;
import com.homeexpress.home_express_api.entity.*;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.exception.UnauthorizedException;
import com.homeexpress.home_express_api.repository.BookingRepository;
import com.homeexpress.home_express_api.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Transactional
    public IncidentResponse createIncident(IncidentRequest request, Long reporterUserId, UserRole reporterRole) {
        if (reporterRole != UserRole.CUSTOMER && reporterRole != UserRole.TRANSPORT) {
            throw new UnauthorizedException("Only CUSTOMER or TRANSPORT users can report incidents");
        }

        Booking booking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + request.getBookingId()));

        if (reporterRole == UserRole.CUSTOMER && !booking.getCustomerId().equals(reporterUserId)) {
            throw new UnauthorizedException("You can only report incidents for your own bookings");
        }

        if (reporterRole == UserRole.TRANSPORT && !reporterUserId.equals(booking.getTransportId())) {
            throw new UnauthorizedException("You can only report incidents for bookings you are assigned to");
        }

        Incident incident = new Incident();
        incident.setBookingId(request.getBookingId());
        incident.setReportedByUserId(reporterUserId);
        incident.setIncidentType(request.getIncidentType());
        incident.setSeverity(request.getSeverity());
        incident.setDescription(request.getDescription());
        incident.setStatus(IncidentStatus.REPORTED);

        Incident savedIncident = incidentRepository.save(incident);
        return IncidentResponse.fromEntity(savedIncident);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncidentById(Long incidentId, Long userId, UserRole userRole) {
        Incident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + incidentId));

        if (userRole != UserRole.MANAGER) {
            Booking booking = bookingRepository.findById(incident.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated booking not found"));

            boolean isCustomer = userRole == UserRole.CUSTOMER && booking.getCustomerId().equals(userId);
            boolean isTransport = userRole == UserRole.TRANSPORT && userId.equals(booking.getTransportId());

            if (!isCustomer && !isTransport) {
                throw new UnauthorizedException("You are not authorized to view this incident");
            }
        }

        return IncidentResponse.fromEntity(incident);
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getIncidents(Long bookingId, IncidentStatus status, 
                                               Severity severity, Long userId, UserRole userRole) {
        List<Incident> incidents;

        if (userRole == UserRole.MANAGER) {
            if (bookingId != null && status != null) {
                incidents = incidentRepository.findByBookingIdAndStatusOrderByReportedAtDesc(bookingId, status);
            } else if (bookingId != null) {
                incidents = incidentRepository.findByBookingIdOrderByReportedAtDesc(bookingId);
            } else if (status != null) {
                incidents = incidentRepository.findByStatusOrderByReportedAtDesc(status);
            } else if (severity != null) {
                incidents = incidentRepository.findBySeverityOrderByReportedAtDesc(severity);
            } else {
                incidents = incidentRepository.findAllByOrderByReportedAtDesc();
            }
        } else if (userRole == UserRole.CUSTOMER) {
            if (bookingId != null) {
                Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
                
                if (!booking.getCustomerId().equals(userId)) {
                    throw new UnauthorizedException("You can only view incidents for your own bookings");
                }
                
                incidents = incidentRepository.findByBookingIdOrderByReportedAtDesc(bookingId);
            } else {
                List<Long> userBookingIds = bookingRepository.findByCustomerIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .map(Booking::getBookingId)
                    .collect(Collectors.toList());
                
                incidents = incidentRepository.findAllByOrderByReportedAtDesc()
                    .stream()
                    .filter(incident -> userBookingIds.contains(incident.getBookingId()))
                    .collect(Collectors.toList());
            }
        } else if (userRole == UserRole.TRANSPORT) {
            List<Long> transportBookingIds = bookingRepository.findAll()
                .stream()
                .filter(booking -> userId.equals(booking.getTransportId()))
                .map(Booking::getBookingId)
                .collect(Collectors.toList());
            
            incidents = incidentRepository.findAllByOrderByReportedAtDesc()
                .stream()
                .filter(incident -> transportBookingIds.contains(incident.getBookingId()))
                .collect(Collectors.toList());
        } else {
            throw new UnauthorizedException("Invalid user role");
        }

        return incidents.stream()
            .map(IncidentResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public IncidentResponse updateIncidentStatus(Long incidentId, IncidentUpdateRequest request, 
                                                 Long userId, UserRole userRole) {
        if (userRole != UserRole.MANAGER) {
            throw new UnauthorizedException("Only managers can update incident status");
        }

        Incident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + incidentId));

        incident.setStatus(request.getStatus());
        
        if (request.getResolutionNotes() != null) {
            incident.setResolutionNotes(request.getResolutionNotes());
        }

        if (request.getStatus() == IncidentStatus.RESOLVED || request.getStatus() == IncidentStatus.CLOSED) {
            incident.setResolvedBy(userId);
            incident.setResolvedAt(LocalDateTime.now());
        }

        Incident updatedIncident = incidentRepository.save(incident);
        return IncidentResponse.fromEntity(updatedIncident);
    }
}
