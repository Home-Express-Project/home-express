package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_settings")
public class CustomerSettings extends SharedPrimaryKeyEntity<Long> {

    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "language", length = 10)
    private String language = "vi";

    @Column(name = "email_notifications")
    private Boolean emailNotifications = Boolean.TRUE;

    @Column(name = "booking_updates")
    private Boolean bookingUpdates = Boolean.TRUE;

    @Column(name = "quotation_alerts")
    private Boolean quotationAlerts = Boolean.TRUE;

    @Column(name = "promotions")
    private Boolean promotions = Boolean.FALSE;

    @Column(name = "newsletter")
    private Boolean newsletter = Boolean.FALSE;

    @Column(name = "profile_visibility", length = 20)
    private String profileVisibility = "public";

    @Column(name = "show_phone")
    private Boolean showPhone = Boolean.TRUE;

    @Column(name = "show_email")
    private Boolean showEmail = Boolean.FALSE;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public CustomerSettings() {
    }

    public CustomerSettings(Customer customer) {
        this.customer = customer;
        this.customerId = customer != null ? customer.getCustomerId() : null;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public Long getId() {
        return customerId;
    }
}
