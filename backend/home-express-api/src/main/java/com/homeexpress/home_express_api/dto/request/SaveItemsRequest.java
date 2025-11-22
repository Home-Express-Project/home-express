package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO for saving multiple items at once
 */
public class SaveItemsRequest {

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<SaveItemRequest> items;

    public List<SaveItemRequest> getItems() {
        return items;
    }

    public void setItems(List<SaveItemRequest> items) {
        this.items = items;
    }
}
