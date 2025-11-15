package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a counter-offer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCounterOfferRequest {

    @NotNull(message = "Quotation ID is required")
    private Long quotationId;

    @NotNull(message = "Offered price is required")
    @DecimalMin(value = "0.01", message = "Offered price must be greater than 0")
    private BigDecimal offeredPrice;

    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    /**
     * Optional: Custom expiration time in hours (default: 24 hours)
     */
    private Integer expirationHours;
}

