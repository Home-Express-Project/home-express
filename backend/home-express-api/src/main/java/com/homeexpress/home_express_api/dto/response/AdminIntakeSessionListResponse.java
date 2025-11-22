package com.homeexpress.home_express_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * List response for admin intake sessions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminIntakeSessionListResponse {
    private List<AdminIntakeSessionDetailResponse> sessions;
    private Integer total;
    private Integer page;
    private Integer size;
}

