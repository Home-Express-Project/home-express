package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a message to a dispute thread.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddDisputeMessageRequest {

    @NotBlank(message = "Message text is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String messageText;
}

