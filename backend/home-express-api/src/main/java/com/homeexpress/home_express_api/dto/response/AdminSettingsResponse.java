package com.homeexpress.home_express_api.dto.response;

public class AdminSettingsResponse {
    private Long managerId;
    private String fullName;
    private String phone;
    private Boolean emailNotifications;
    private Boolean systemAlerts;
    private Boolean userRegistrations;
    private Boolean transportVerifications;
    private Boolean bookingAlerts;
    private Boolean reviewModeration;
    private Boolean twoFactorEnabled;
    private Integer sessionTimeoutMinutes;
    private Boolean loginNotifications;
    private String theme;
    private String dateFormat;
    private String timezone;
    private Boolean maintenanceMode;
    private Boolean autoBackup;
    private String backupFrequency;
    private String emailProvider;
    private String smtpHost;
    private String smtpPort;
    private String smtpUsername;

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
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
}

