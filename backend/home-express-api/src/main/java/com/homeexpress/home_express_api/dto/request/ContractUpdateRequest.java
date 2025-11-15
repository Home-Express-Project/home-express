package com.homeexpress.home_express_api.dto.request;

import com.homeexpress.home_express_api.entity.ContractStatus;

import jakarta.validation.constraints.NotNull;

public class ContractUpdateRequest {

    @NotNull(message = "Status is required")
    private ContractStatus status;

    public ContractUpdateRequest() {
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }
}
