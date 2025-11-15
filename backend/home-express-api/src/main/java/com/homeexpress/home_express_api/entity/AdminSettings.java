package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_settings")
public class AdminSettings extends SharedPrimaryKeyEntity<Long> {

    @Id
    @Column(name = "manager_id")
    private Long managerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "manager_id")
    private Manager manager;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email_notifications")
    private Boolean emailNotifications = Boolean.TRUE;

    @Column(name = "system_alerts")
    private Boolean systemAlerts = Boolean.TRUE;

    @Column(name = "user_registrations")
    private Boolean userRegistrations = Boolean.TRUE;

    @Column(name = "transport_verifications")
    private Boolean transportVerifications = Boolean.TRUE;

    @Column(name = "booking_alerts")
    private Boolean bookingAlerts = Boolean.FALSE;

    @Column(name = "review_moderation")
    private Boolean reviewModeration = Boolean.TRUE;

    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = Boolean.FALSE;

    @Column(name = "session_timeout_minutes")
    private Integer sessionTimeoutMinutes = 30;

    @Column(name = "login_notifications")
    private Boolean loginNotifications = Boolean.TRUE;

    @Column(name = "theme", length = 10)
    private String theme = "light";

    @Column(name = "date_format", length = 20)
    private String dateFormat = "DD/MM/YYYY";

    @Column(name = "timezone", length = 100)
    private String timezone = "Asia/Ho_Chi_Minh";

    @Column(name = "maintenance_mode")
    private Boolean maintenanceMode = Boolean.FALSE;

    @Column(name = "auto_backup")
    private Boolean autoBackup = Boolean.TRUE;

    @Column(name = "backup_frequency", length = 10)
    private String backupFrequency = "daily";

    @Column(name = "email_provider", length = 20)
    private String emailProvider = "smtp";

    @Column(name = "smtp_host", length = 255)
    private String smtpHost;

    @Column(name = "smtp_port", length = 10)
    private String smtpPort;

    @Column(name = "smtp_username", length = 255)
    private String smtpUsername;

    @Column(name = "smtp_password", columnDefinition = "TEXT")
    private String smtpPassword;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public AdminSettings() {
    }

    public AdminSettings(Manager manager) {
        this.manager = manager;
        this.managerId = manager != null ? manager.getManagerId() : null;
        if (manager != null) {
            this.fullName = manager.getFullName();
            this.phone = manager.getPhone();
        }
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public Boolean getSystemAlerts() {
        return systemAlerts;
    }

    public void setSystemAlerts(Boolean systemAlerts) {
        this.systemAlerts = systemAlerts;
    }

    public Boolean getUserRegistrations() {
        return userRegistrations;
    }

    public void setUserRegistrations(Boolean userRegistrations) {
        this.userRegistrations = userRegistrations;
    }

    public Boolean getTransportVerifications() {
        return transportVerifications;
    }

    public void setTransportVerifications(Boolean transportVerifications) {
        this.transportVerifications = transportVerifications;
    }

    public Boolean getBookingAlerts() {
        return bookingAlerts;
    }

    public void setBookingAlerts(Boolean bookingAlerts) {
        this.bookingAlerts = bookingAlerts;
    }

    public Boolean getReviewModeration() {
        return reviewModeration;
    }

    public void setReviewModeration(Boolean reviewModeration) {
        this.reviewModeration = reviewModeration;
    }

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public Integer getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    public void setSessionTimeoutMinutes(Integer sessionTimeoutMinutes) {
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    public Boolean getLoginNotifications() {
        return loginNotifications;
    }

    public void setLoginNotifications(Boolean loginNotifications) {
        this.loginNotifications = loginNotifications;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Boolean getMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(Boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    public Boolean getAutoBackup() {
        return autoBackup;
    }

    public void setAutoBackup(Boolean autoBackup) {
        this.autoBackup = autoBackup;
    }

    public String getBackupFrequency() {
        return backupFrequency;
    }

    public void setBackupFrequency(String backupFrequency) {
        this.backupFrequency = backupFrequency;
    }

    public String getEmailProvider() {
        return emailProvider;
    }

    public void setEmailProvider(String emailProvider) {
        this.emailProvider = emailProvider;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public void setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public Long getId() {
        return managerId;
    }
}
