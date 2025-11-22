package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for responding to a counter-offer (accept or reject)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespondToCounterOfferRequest {

    @NotNull(message = "Accept flag is required")
    private Boolean accept;

    @Size(max = 1000, message = "Response message cannot exceed 1000 characters")
    private String responseMessage;
}

