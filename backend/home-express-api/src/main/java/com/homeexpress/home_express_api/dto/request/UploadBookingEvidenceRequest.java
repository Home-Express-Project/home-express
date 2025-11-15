package com.homeexpress.home_express_api.dto.request;

import com.homeexpress.home_express_api.entity.EvidenceType;
import com.homeexpress.home_express_api.entity.FileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for uploading booking evidence
 */
public class UploadBookingEvidenceRequest {

    @NotNull(message = "Evidence type is required")
    private EvidenceType evidenceType;

    @NotNull(message = "File type is required")
    private FileType fileType;

    @NotBlank(message = "File URL is required")
    private String fileUrl;

    @NotBlank(message = "File name is required")
    @Size(max = 500, message = "File name must not exceed 500 characters")
    private String fileName;

    private String mimeType;

    private Long fileSizeBytes;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    public UploadBookingEvidenceRequest() {
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
}

