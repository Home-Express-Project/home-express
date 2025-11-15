package com.homeexpress.home_express_api.dto.request;

import com.homeexpress.home_express_api.dto.ai.DetectionResult;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload to persist detected items into booking_items table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectedItemsPersistRequest {

    @NotNull(message = "Detection result is required")
    private DetectionResult detectionResult;

    /**
     * Whether to replace existing booking items for the booking.
     * Defaults to {@code true} when not provided.
     */
    private Boolean replaceExisting = Boolean.TRUE;

    public boolean isReplaceExisting() {
        return replaceExisting == null || replaceExisting;
    }
}
