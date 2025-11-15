package com.homeexpress.home_express_api.dto.response;

import com.homeexpress.home_express_api.entity.Evidence;
import com.homeexpress.home_express_api.entity.EvidenceType;
import com.homeexpress.home_express_api.entity.FileType;
import java.time.LocalDateTime;

/**
 * Response DTO for booking evidence
 */
public class BookingEvidenceResponse {

    private Long evidenceId;
    private Long bookingId;
    private Long uploadedByUserId;
    private String uploaderName;
    private String uploaderRole;
    private EvidenceType evidenceType;
    private FileType fileType;
    private String fileUrl;
    private String fileName;
    private String mimeType;
    private Long fileSizeBytes;
    private String description;
    private LocalDateTime uploadedAt;

    public BookingEvidenceResponse() {
    }

    public static BookingEvidenceResponse fromEntity(Evidence evidence) {
        BookingEvidenceResponse response = new BookingEvidenceResponse();
        response.setEvidenceId(evidence.getEvidenceId());
        response.setBookingId(evidence.getBookingId());
        response.setUploadedByUserId(evidence.getUploadedByUserId());
        response.setEvidenceType(evidence.getEvidenceType());
        response.setFileType(evidence.getFileType());
        response.setFileUrl(evidence.getFileUrl());
        response.setFileName(evidence.getFileName());
        response.setMimeType(evidence.getMimeType());
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

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getUploadedByUserId() {
        return uploadedByUserId;
    }

    public void setUploadedByUserId(Long uploadedByUserId) {
        this.uploadedByUserId = uploadedByUserId;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUploaderRole() {
        return uploaderRole;
    }

    public void setUploaderRole(String uploaderRole) {
        this.uploaderRole = uploaderRole;
    }

    public EvidenceType getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(EvidenceType evidenceType) {
        this.evidenceType = evidenceType;
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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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

