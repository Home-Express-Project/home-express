package com.homeexpress.home_express_api.dto.incident;

import com.homeexpress.home_express_api.entity.IncidentStatus;
import jakarta.validation.constraints.NotNull;

public class IncidentUpdateRequest {

    @NotNull(message = "Status is required")
    private IncidentStatus status;

    private String resolutionNotes;

    public IncidentUpdateRequest() {
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
