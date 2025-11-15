package com.homeexpress.home_express_api.dto.intake;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request to merge item candidates into an intake session
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntakeMergeRequest {
    
    @NotEmpty(message = "Candidates list cannot be empty")
    @Valid
    private List<ItemCandidateDto> candidates;
}
