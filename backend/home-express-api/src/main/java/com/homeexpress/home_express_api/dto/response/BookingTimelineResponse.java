package com.homeexpress.home_express_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response for booking timeline endpoint
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingTimelineResponse {
    private Long bookingId;
    private List<BookingTimelineEvent> timeline;
    private Integer totalEvents;
}

