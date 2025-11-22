package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.request.UpdateAdminSettingsRequest;
import com.homeexpress.home_express_api.dto.response.AdminSettingsResponse;
import com.homeexpress.home_express_api.entity.AdminSettings;
import com.homeexpress.home_express_api.entity.Manager;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.repository.AdminSettingsRepository;
import com.homeexpress.home_express_api.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminSettingsService {

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private AdminSettingsRepository adminSettingsRepository;

    @Transactional(readOnly = true)
    public AdminSettingsResponse getSettings(Long managerId) {
        AdminSettings settings = adminSettingsRepository.findById(managerId)
                .orElseGet(() -> createDefaultSettings(managerId));
        return mapToResponse(settings);
    }

    @Transactional
    public AdminSettingsResponse updateSettings(Long managerId, UpdateAdminSettingsRequest request) {
        AdminSettings settings = adminSettingsRepository.findById(managerId)
                .orElseGet(() -> createDefaultSettings(managerId));

        if (request.getFullName() != null) {
            settings.setFullName(request.getFullName());
            settings.getManager().setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            settings.setPhone(request.getPhone());
            settings.getManager().setPhone(request.getPhone());
        }
        if (request.getEmailNotifications() != null) {
            settings.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getSystemAlerts() != null) {
            settings.setSystemAlerts(request.getSystemAlerts());
        }
        if (request.getUserRegistrations() != null) {
            settings.setUserRegistrations(request.getUserRegistrations());
        }
        if (request.getTransportVerifications() != null) {
            settings.setTransportVerifications(request.getTransportVerifications());
        }
        if (request.getBookingAlerts() != null) {
            settings.setBookingAlerts(request.getBookingAlerts());
        }
        if (request.getReviewModeration() != null) {
            settings.setReviewModeration(request.getReviewModeration());
        }
        if (request.getTwoFactorEnabled() != null) {
            settings.setTwoFactorEnabled(request.getTwoFactorEnabled());
        }
        if (request.getSessionTimeoutMinutes() != null) {
            settings.setSessionTimeoutMinutes(request.getSessionTimeoutMinutes());
        }
        if (request.getLoginNotifications() != null) {
            settings.setLoginNotifications(request.getLoginNotifications());
        }
        if (request.getTheme() != null) {
            settings.setTheme(request.getTheme());
        }
        if (request.getDateFormat() != null) {
            settings.setDateFormat(request.getDateFormat());
        }
        if (request.getTimezone() != null) {
            settings.setTimezone(request.getTimezone());
        }
        if (request.getMaintenanceMode() != null) {
            settings.setMaintenanceMode(request.getMaintenanceMode());
        }
        if (request.getAutoBackup() != null) {
            settings.setAutoBackup(request.getAutoBackup());
        }
        if (request.getBackupFrequency() != null) {
            settings.setBackupFrequency(request.getBackupFrequency());
        }
        if (request.getEmailProvider() != null) {
            settings.setEmailProvider(request.getEmailProvider());
        }
        if (request.getSmtpHost() != null) {
            settings.setSmtpHost(request.getSmtpHost());
        }
        if (request.getSmtpPort() != null) {
            settings.setSmtpPort(request.getSmtpPort());
        }
        if (request.getSmtpUsername() != null) {
            settings.setSmtpUsername(request.getSmtpUsername());
        }
        if (request.getSmtpPassword() != null) {
            settings.setSmtpPassword(request.getSmtpPassword());
        }

        AdminSettings saved = adminSettingsRepository.save(settings);
        return mapToResponse(saved);
    }

    private AdminSettings createDefaultSettings(Long managerId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", managerId));
        AdminSettings settings = new AdminSettings(manager);
        return adminSettingsRepository.save(settings);
    }

    private AdminSettingsResponse mapToResponse(AdminSettings settings) {
        AdminSettingsResponse response = new AdminSettingsResponse();
        response.setManagerId(settings.getManagerId());
        response.setFullName(settings.getFullName());
        response.setPhone(settings.getPhone());
        response.setEmailNotifications(settings.getEmailNotifications());
        response.setSystemAlerts(settings.getSystemAlerts());
        response.setUserRegistrations(settings.getUserRegistrations());
        response.setTransportVerifications(settings.getTransportVerifications());
        response.setBookingAlerts(settings.getBookingAlerts());
        response.setReviewModeration(settings.getReviewModeration());
        response.setTwoFactorEnabled(settings.getTwoFactorEnabled());
        response.setSessionTimeoutMinutes(settings.getSessionTimeoutMinutes());
        response.setLoginNotifications(settings.getLoginNotifications());
        response.setTheme(settings.getTheme());
        response.setDateFormat(settings.getDateFormat());
        response.setTimezone(settings.getTimezone());
        response.setMaintenanceMode(settings.getMaintenanceMode());
        response.setAutoBackup(settings.getAutoBackup());
        response.setBackupFrequency(settings.getBackupFrequency());
        response.setEmailProvider(settings.getEmailProvider());
        response.setSmtpHost(settings.getSmtpHost());
        response.setSmtpPort(settings.getSmtpPort());
        response.setSmtpUsername(settings.getSmtpUsername());
        return response;
    }
}

