package com.homeexpress.home_express_api.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CustomerSettingsResponse {
    private Long customerId;
    private String language;
    private Boolean emailNotifications;
    private Boolean bookingUpdates;
    private Boolean quotationAlerts;
    private Boolean promotions;
    private Boolean newsletter;
    private String profileVisibility;
    private Boolean showPhone;
    private Boolean showEmail;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public Boolean getBookingUpdates() {
        return bookingUpdates;
    }

    public void setBookingUpdates(Boolean bookingUpdates) {
        this.bookingUpdates = bookingUpdates;
    }

    public Boolean getQuotationAlerts() {
        return quotationAlerts;
    }

    public void setQuotationAlerts(Boolean quotationAlerts) {
        this.quotationAlerts = quotationAlerts;
    }

    public Boolean getPromotions() {
        return promotions;
    }

    public void setPromotions(Boolean promotions) {
        this.promotions = promotions;
    }

    public Boolean getNewsletter() {
        return newsletter;
    }

    public void setNewsletter(Boolean newsletter) {
        this.newsletter = newsletter;
    }

    public String getProfileVisibility() {
        return profileVisibility;
    }

    public void setProfileVisibility(String profileVisibility) {
        this.profileVisibility = profileVisibility;
    }

    public Boolean getShowPhone() {
        return showPhone;
    }

    public void setShowPhone(Boolean showPhone) {
        this.showPhone = showPhone;
    }

    public Boolean getShowEmail() {
        return showEmail;
    }

    public void setShowEmail(Boolean showEmail) {
        this.showEmail = showEmail;
    }
}

