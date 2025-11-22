package com.homeexpress.home_express_api.dto.admin;

import jakarta.validation.constraints.NotNull;

public class CreatePayoutRequest {

    @NotNull(message = "Transport ID is required")
    private Long transportId;

    private Boolean allTransports = false;

    public CreatePayoutRequest() {
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public Boolean getAllTransports() {
        return allTransports;
    }

    public void setAllTransports(Boolean allTransports) {
        this.allTransports = allTransports;
    }
}
