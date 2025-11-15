package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class NotificationPreferenceRequest {

    @NotNull
    private Boolean emailEnabled;

    @NotNull
    private Boolean smsEnabled;

    @NotNull
    private Boolean pushEnabled;

    @NotNull
    private Boolean inAppEnabled;

    private LocalTime quietHoursStart;

    private LocalTime quietHoursEnd;

    @NotNull
    private Boolean bookingUpdatesEnabled;

    @NotNull
    private Boolean quotationAlertsEnabled;

    @NotNull
    private Boolean paymentRemindersEnabled;

    @NotNull
    private Boolean systemAlertsEnabled;

    @NotNull
    private Boolean promotionsEnabled;

    public NotificationPreferenceRequest() {
    }

    public Boolean getEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(Boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public Boolean getSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(Boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }

    public Boolean getPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(Boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public Boolean getInAppEnabled() {
        return inAppEnabled;
    }

    public void setInAppEnabled(Boolean inAppEnabled) {
        this.inAppEnabled = inAppEnabled;
    }

    public LocalTime getQuietHoursStart() {
        return quietHoursStart;
    }

    public void setQuietHoursStart(LocalTime quietHoursStart) {
        this.quietHoursStart = quietHoursStart;
    }

    public LocalTime getQuietHoursEnd() {
        return quietHoursEnd;
    }

    public void setQuietHoursEnd(LocalTime quietHoursEnd) {
        this.quietHoursEnd = quietHoursEnd;
    }

    public Boolean getBookingUpdatesEnabled() {
        return bookingUpdatesEnabled;
    }

    public void setBookingUpdatesEnabled(Boolean bookingUpdatesEnabled) {
        this.bookingUpdatesEnabled = bookingUpdatesEnabled;
    }

    public Boolean getQuotationAlertsEnabled() {
        return quotationAlertsEnabled;
    }

    public void setQuotationAlertsEnabled(Boolean quotationAlertsEnabled) {
        this.quotationAlertsEnabled = quotationAlertsEnabled;
    }

    public Boolean getPaymentRemindersEnabled() {
        return paymentRemindersEnabled;
    }

    public void setPaymentRemindersEnabled(Boolean paymentRemindersEnabled) {
        this.paymentRemindersEnabled = paymentRemindersEnabled;
    }

    public Boolean getSystemAlertsEnabled() {
        return systemAlertsEnabled;
    }

    public void setSystemAlertsEnabled(Boolean systemAlertsEnabled) {
        this.systemAlertsEnabled = systemAlertsEnabled;
    }

    public Boolean getPromotionsEnabled() {
        return promotionsEnabled;
    }

    public void setPromotionsEnabled(Boolean promotionsEnabled) {
        this.promotionsEnabled = promotionsEnabled;
    }
}
