package com.homeexpress.home_express_api.dto.request;

import com.homeexpress.home_express_api.entity.DisputeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a new dispute.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDisputeRequest {

    @NotNull(message = "Dispute type is required")
    private DisputeType disputeType;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 2000, message = "Requested resolution must not exceed 2000 characters")
    private String requestedResolution;

    /**
     * List of evidence IDs to attach to the dispute.
     * Evidence must belong to the same booking.
     */
    private List<Long> evidenceIds;
}

