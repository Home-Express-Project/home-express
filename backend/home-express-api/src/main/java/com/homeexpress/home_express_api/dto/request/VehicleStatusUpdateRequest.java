package com.homeexpress.home_express_api.dto.request;

import com.homeexpress.home_express_api.entity.VehicleStatus;
import jakarta.validation.constraints.NotNull;

public class VehicleStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private VehicleStatus status;

    public VehicleStatusUpdateRequest() {}

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }
}
