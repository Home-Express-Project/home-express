package com.homeexpress.home_express_api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponse {
    private UserSummaryResponse user;
    private CustomerProfileResponse customer;
    private TransportProfileResponse transport;
    private ManagerProfileResponse manager;

    public UserSummaryResponse getUser() {
        return user;
    }

    public void setUser(UserSummaryResponse user) {
        this.user = user;
    }

    public CustomerProfileResponse getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerProfileResponse customer) {
        this.customer = customer;
    }

    public TransportProfileResponse getTransport() {
        return transport;
    }

    public void setTransport(TransportProfileResponse transport) {
        this.transport = transport;
    }

    public ManagerProfileResponse getManager() {
        return manager;
    }

    public void setManager(ManagerProfileResponse manager) {
        this.manager = manager;
    }
}

