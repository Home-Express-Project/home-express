package com.homeexpress.home_express_api.dto.response;

import java.math.BigDecimal;

public class TransportSettingsResponse {

    private String verificationStatus; // PENDING, APPROVED, REJECTED
    private BigDecimal searchRadiusKm;
    private Long minJobValueVnd;
    private Boolean autoAcceptJobs;
    private BigDecimal responseTimeHours;
    private Boolean emailNotifications;
    private Boolean newJobAlerts;
    private Boolean quotationUpdates;
    private Boolean paymentNotifications;
    private Boolean reviewNotifications;

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
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

    public Boolean getAutoAcceptJobs() {
        return autoAcceptJobs;
    }

    public void setAutoAcceptJobs(Boolean autoAcceptJobs) {
        this.autoAcceptJobs = autoAcceptJobs;
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
