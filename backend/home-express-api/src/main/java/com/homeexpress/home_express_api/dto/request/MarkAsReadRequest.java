package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class MarkAsReadRequest {

    @NotEmpty
    private List<Long> notificationIds;

    public MarkAsReadRequest() {
    }

    public List<Long> getNotificationIds() {
        return notificationIds;
    }

    public void setNotificationIds(List<Long> notificationIds) {
        this.notificationIds = notificationIds;
    }
}
