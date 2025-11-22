package com.homeexpress.home_express_api.dto.request;

import com.homeexpress.home_express_api.entity.DisputeStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating dispute status.
 * Only managers can update dispute status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDisputeStatusRequest {

    @NotNull(message = "Status is required")
    private DisputeStatus status;

    @Size(max = 2000, message = "Resolution notes must not exceed 2000 characters")
    private String resolutionNotes;
}

