package com.homeexpress.home_express_api.dto.intake;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response after merging item candidates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntakeMergeResponse {
    
    private String sessionId;
    private Integer itemCount;
    private String message;
}
