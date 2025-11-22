package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transport_settings")
public class TransportSettings extends SharedPrimaryKeyEntity<Long> {

    @Id
    @Column(name = "transport_id")
    private Long transportId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "transport_id")
    private Transport transport;

    @Column(name = "search_radius_km", precision = 5, scale = 2)
    private BigDecimal searchRadiusKm = BigDecimal.valueOf(10.0);

    @Column(name = "min_job_value_vnd", precision = 12, scale = 0)
    private Long minJobValueVnd = 0L;

    @Column(name = "auto_accept_jobs")
    private Boolean autoAcceptJobs = Boolean.FALSE;

    @Column(name = "response_time_hours", precision = 4, scale = 1)
    private BigDecimal responseTimeHours = BigDecimal.valueOf(2.0);

    @Column(name = "email_notifications")
    private Boolean emailNotifications = Boolean.TRUE;

    @Column(name = "new_job_alerts")
    private Boolean newJobAlerts = Boolean.TRUE;

    @Column(name = "quotation_updates")
    private Boolean quotationUpdates = Boolean.TRUE;

    @Column(name = "payment_notifications")
    private Boolean paymentNotifications = Boolean.TRUE;

    @Column(name = "review_notifications")
    private Boolean reviewNotifications = Boolean.TRUE;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public TransportSettings() {
    }

    public TransportSettings(Transport transport) {
        this.transport = transport;
        this.transportId = transport != null ? transport.getTransportId() : null;
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public Long getId() {
        return transportId;
    }
}
