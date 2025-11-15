package com.homeexpress.home_express_api.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class UpdateTransportSettingsRequest {

    private Boolean autoAcceptJobs;

    @DecimalMin(value = "0.0", inclusive = false, message = "Search radius must be positive")
    private BigDecimal searchRadiusKm;

    @PositiveOrZero(message = "Minimum job value must be positive")
    private Long minJobValueVnd;

    @DecimalMin(value = "0.5", inclusive = true, message = "Response time must be at least 0.5h")
    private BigDecimal responseTimeHours;

    private Boolean emailNotifications;
    private Boolean newJobAlerts;
    private Boolean quotationUpdates;
    private Boolean paymentNotifications;
    private Boolean reviewNotifications;

    public Boolean getAutoAcceptJobs() {
        return autoAcceptJobs;
    }

    public void setAutoAcceptJobs(Boolean autoAcceptJobs) {
        this.autoAcceptJobs = autoAcceptJobs;
    }

    public BigDecimal getSearchRadiusKm() {
        return searchRadiusKm;
    }

    public void setSearchRadiusKm(BigDecimal searchRadiusKm) {
        this.searchRadiusKm = searchRadiusKm;
    }

    public Long getMinJobValueVnd() {
        return minJobValueVnd;
    }

    public void setMinJobValueVnd(Long minJobValueVnd) {
        this.minJobValueVnd = minJobValueVnd;
    }

    public BigDecimal getResponseTimeHours() {
        return responseTimeHours;
    }

    public void setResponseTimeHours(BigDecimal responseTimeHours) {
        this.responseTimeHours = responseTimeHours;
    }

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public Boolean getNewJobAlerts() {
        return newJobAlerts;
    }

    public void setNewJobAlerts(Boolean newJobAlerts) {
        this.newJobAlerts = newJobAlerts;
    }

    public Boolean getQuotationUpdates() {
        return quotationUpdates;
    }

    public void setQuotationUpdates(Boolean quotationUpdates) {
        this.quotationUpdates = quotationUpdates;
    }

    public Boolean getPaymentNotifications() {
        return paymentNotifications;
    }

    public void setPaymentNotifications(Boolean paymentNotifications) {
        this.paymentNotifications = paymentNotifications;
    }

    public Boolean getReviewNotifications() {
        return reviewNotifications;
    }

    public void setReviewNotifications(Boolean reviewNotifications) {
        this.reviewNotifications = reviewNotifications;
    }
}

