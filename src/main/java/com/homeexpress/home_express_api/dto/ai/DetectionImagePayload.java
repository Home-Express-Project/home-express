package com.homeexpress.home_express_api.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Payload describing an image submitted to the AI detection pipeline.
 * Supports both remote URLs and binary uploads.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionImagePayload {

    /**
     * Binary image data (optional when using an external URL).
     */
    private byte[] data;

    /**
     * MIME type of the image (e.g. {@code image/jpeg}).
     */
    private String contentType;

    /**
     * Original filename supplied by the client (optional).
     */
    private String originalFilename;

    /**
     * External URL that the AI service can download (optional).
     */
    private String externalUrl;

    /**
     * @return {@code true} when binary data is present.
     */
    public boolean hasBinary() {
        return data != null && data.length > 0;
    }

    /**
     * Resolve a human-readable reference for logging/metadata.
     */
    public String getReferenceLabel(int index) {
        if (StringUtils.hasText(externalUrl)) {
            return externalUrl;
        }
        if (StringUtils.hasText(originalFilename)) {
            return originalFilename;
        }
        String checksum = computeChecksumSafe();
        if (checksum != null) {
            return "upload://" + index + "/" + checksum.substring(0, Math.min(12, checksum.length()));
        }
        return "upload://" + index;
    }

    /**
     * Compute a SHA-256 checksum of the binary payload.
     */
    public String computeChecksumSafe() {
        if (!hasBinary()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to a simple hash code if SHA-256 is unavailable (extremely unlikely)
            return Integer.toHexString(new String(data, StandardCharsets.ISO_8859_1).hashCode());
        }
    }

    /**
     * Resolve the MIME type, defaulting to {@code image/jpeg} when unspecified.
     */
    public String resolveContentType() {
        if (StringUtils.hasText(contentType)) {
            return contentType;
        }
        return "image/jpeg";
    }
}
