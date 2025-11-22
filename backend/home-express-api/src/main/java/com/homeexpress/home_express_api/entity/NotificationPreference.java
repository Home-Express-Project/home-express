package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long preferenceId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull
    private User user;

    @NotNull
    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @NotNull
    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = false;

    @NotNull
    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = true;

    @NotNull
    @Column(name = "in_app_enabled", nullable = false)
    private Boolean inAppEnabled = true;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @NotNull
    @Column(name = "booking_updates_enabled", nullable = false)
    private Boolean bookingUpdatesEnabled = true;

    @NotNull
    @Column(name = "quotation_alerts_enabled", nullable = false)
    private Boolean quotationAlertsEnabled = true;

    @NotNull
    @Column(name = "payment_reminders_enabled", nullable = false)
    private Boolean paymentRemindersEnabled = true;

    @NotNull
    @Column(name = "system_alerts_enabled", nullable = false)
    private Boolean systemAlertsEnabled = true;

    @NotNull
    @Column(name = "promotions_enabled", nullable = false)
    private Boolean promotionsEnabled = true;

    public NotificationPreference() {
    }

    public Long getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(Long preferenceId) {
        this.preferenceId = preferenceId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
