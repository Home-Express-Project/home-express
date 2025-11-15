package com.homeexpress.home_express_api.dto.payout;

/**
 * DTO for batch payout creation requests.
 * Used when creating payouts for specific transports or all transports.
 */
public class PayoutBatchRequestDTO {

    private Long transportId;
    private String notes;

    public PayoutBatchRequestDTO() {
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
