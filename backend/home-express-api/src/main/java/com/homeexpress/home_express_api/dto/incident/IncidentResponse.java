package com.homeexpress.home_express_api.dto.incident;

import com.homeexpress.home_express_api.entity.Incident;
import com.homeexpress.home_express_api.entity.IncidentStatus;
import com.homeexpress.home_express_api.entity.IncidentType;
import com.homeexpress.home_express_api.entity.Severity;
import java.time.LocalDateTime;

public class IncidentResponse {

    private Long incidentId;
    private Long bookingId;
    private Long reportedByUserId;
    private IncidentType incidentType;
    private Severity severity;
    private String description;
    private IncidentStatus status;
    private String resolutionNotes;
    private Long resolvedBy;
    private LocalDateTime resolvedAt;
    private LocalDateTime reportedAt;
    private LocalDateTime updatedAt;

    public IncidentResponse() {
    }

    public static IncidentResponse fromEntity(Incident incident) {
        IncidentResponse response = new IncidentResponse();
        response.setIncidentId(incident.getIncidentId());
        response.setBookingId(incident.getBookingId());
        response.setReportedByUserId(incident.getReportedByUserId());
        response.setIncidentType(incident.getIncidentType());
        response.setSeverity(incident.getSeverity());
        response.setDescription(incident.getDescription());
        response.setStatus(incident.getStatus());
        response.setResolutionNotes(incident.getResolutionNotes());
        response.setResolvedBy(incident.getResolvedBy());
        response.setResolvedAt(incident.getResolvedAt());
        response.setReportedAt(incident.getReportedAt());
        response.setUpdatedAt(incident.getUpdatedAt());
        return response;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(Long incidentId) {
        this.incidentId = incidentId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getReportedByUserId() {
        return reportedByUserId;
    }

    public void setReportedByUserId(Long reportedByUserId) {
        this.reportedByUserId = reportedByUserId;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(IncidentType incidentType) {
        this.incidentType = incidentType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public Long getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Long resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
