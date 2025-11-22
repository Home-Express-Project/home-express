package com.homeexpress.home_express_api.dto.incident;

import com.homeexpress.home_express_api.entity.Evidence;
import com.homeexpress.home_express_api.entity.FileType;
import java.time.LocalDateTime;

public class EvidenceResponse {

    private Long evidenceId;
    private Long incidentId;
    private Long uploadedByUserId;
    private FileType fileType;
    private String fileUrl;
    private String fileName;
    private Long fileSizeBytes;
    private String description;
    private LocalDateTime uploadedAt;

    public EvidenceResponse() {
    }

    public static EvidenceResponse fromEntity(Evidence evidence) {
        EvidenceResponse response = new EvidenceResponse();
        response.setEvidenceId(evidence.getEvidenceId());
        response.setIncidentId(evidence.getIncidentId());
        response.setUploadedByUserId(evidence.getUploadedByUserId());
        response.setFileType(evidence.getFileType());
        response.setFileUrl(evidence.getFileUrl());
        response.setFileName(evidence.getFileName());
        response.setFileSizeBytes(evidence.getFileSizeBytes());
        response.setDescription(evidence.getDescription());
        response.setUploadedAt(evidence.getUploadedAt());
        return response;
    }

    public Long getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(Long evidenceId) {
        this.evidenceId = evidenceId;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(Long incidentId) {
        this.incidentId = incidentId;
    }

    public Long getUploadedByUserId() {
        return uploadedByUserId;
    }

    public void setUploadedByUserId(Long uploadedByUserId) {
        this.uploadedByUserId = uploadedByUserId;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
