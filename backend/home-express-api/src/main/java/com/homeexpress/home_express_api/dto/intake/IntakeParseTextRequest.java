package com.homeexpress.home_express_api.dto.intake;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for parsing free-form intake text into item candidates.
 */
@Data
public class IntakeParseTextRequest {

    @NotBlank(message = "text is required")
    private String text;
}

