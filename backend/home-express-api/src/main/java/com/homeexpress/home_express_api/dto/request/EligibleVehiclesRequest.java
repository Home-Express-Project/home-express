package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibleVehiclesRequest {
    @NotNull
    private BigDecimal totalWeight;

    @NotNull
    private BigDecimal totalVolume;

    private Boolean requiresTailLift;

    private Boolean requiresTools;
}
