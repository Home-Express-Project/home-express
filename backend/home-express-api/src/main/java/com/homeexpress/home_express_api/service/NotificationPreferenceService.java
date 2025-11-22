package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.request.NotificationPreferenceRequest;
import com.homeexpress.home_express_api.dto.response.NotificationPreferenceResponse;
import com.homeexpress.home_express_api.entity.Notification;
import com.homeexpress.home_express_api.entity.NotificationPreference;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.repository.NotificationPreferenceRepository;
import com.homeexpress.home_express_api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public NotificationPreferenceService(
            NotificationPreferenceRepository preferenceRepository,
            UserRepository userRepository) {
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getUserPreferences(Long userId) {
        NotificationPreference preference = preferenceRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        return mapToResponse(preference);
    }

    @Transactional
    public NotificationPreferenceResponse updateUserPreferences(Long userId, NotificationPreferenceRequest request) {
        NotificationPreference preference = preferenceRepository.findByUser_UserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    NotificationPreference newPref = new NotificationPreference();
                    newPref.setUser(user);
                    return newPref;
                });

        preference.setEmailEnabled(request.getEmailEnabled());
        preference.setSmsEnabled(request.getSmsEnabled());
        preference.setPushEnabled(request.getPushEnabled());
        preference.setInAppEnabled(request.getInAppEnabled());
        preference.setQuietHoursStart(request.getQuietHoursStart());
        preference.setQuietHoursEnd(request.getQuietHoursEnd());
        preference.setBookingUpdatesEnabled(request.getBookingUpdatesEnabled());
        preference.setQuotationAlertsEnabled(request.getQuotationAlertsEnabled());
        preference.setPaymentRemindersEnabled(request.getPaymentRemindersEnabled());
        preference.setSystemAlertsEnabled(request.getSystemAlertsEnabled());
        preference.setPromotionsEnabled(request.getPromotionsEnabled());

        preference = preferenceRepository.save(preference);
        return mapToResponse(preference);
    }

    public boolean shouldSendNotification(Long userId, Notification.NotificationType type) {
        NotificationPreference preference = preferenceRepository.findByUser_UserId(userId)
                .orElse(null);

        if (preference == null) {
            return true;
        }

        if (!preference.getInAppEnabled()) {
            return false;
        }

        if (isInQuietHours(preference)) {
            if (type == Notification.NotificationType.SYSTEM_ALERT) {
                return true;
            }
            return false;
        }

        switch (type) {
            case BOOKING_UPDATE:
            case DELIVERY_STATUS:
            case VEHICLE_ASSIGNMENT:
            case CONTRACT_UPDATE:
                return preference.getBookingUpdatesEnabled();
            case QUOTATION_RECEIVED:
                return preference.getQuotationAlertsEnabled();
            case PAYMENT_REMINDER:
                return preference.getPaymentRemindersEnabled();
            case SYSTEM_ALERT:
                return preference.getSystemAlertsEnabled();
            case PROMOTION:
                return preference.getPromotionsEnabled();
            default:
                return true;
        }
    }

    private boolean isInQuietHours(NotificationPreference preference) {
        LocalTime start = preference.getQuietHoursStart();
        LocalTime end = preference.getQuietHoursEnd();

        if (start == null || end == null) {
            return false;
        }

        LocalTime now = LocalTime.now();

        if (start.isBefore(end)) {
            return now.isAfter(start) && now.isBefore(end);
        } else {
            return now.isAfter(start) || now.isBefore(end);
        }
    }

    @Transactional
    private NotificationPreference createDefaultPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        NotificationPreference preference = new NotificationPreference();
        preference.setUser(user);
        return preferenceRepository.save(preference);
    }

    private NotificationPreferenceResponse mapToResponse(NotificationPreference preference) {
        NotificationPreferenceResponse response = new NotificationPreferenceResponse();
        response.setPreferenceId(preference.getPreferenceId());
        response.setEmailEnabled(preference.getEmailEnabled());
        response.setSmsEnabled(preference.getSmsEnabled());
        response.setPushEnabled(preference.getPushEnabled());
        response.setInAppEnabled(preference.getInAppEnabled());
        response.setQuietHoursStart(preference.getQuietHoursStart());
        response.setQuietHoursEnd(preference.getQuietHoursEnd());
        response.setBookingUpdatesEnabled(preference.getBookingUpdatesEnabled());
        response.setQuotationAlertsEnabled(preference.getQuotationAlertsEnabled());
        response.setPaymentRemindersEnabled(preference.getPaymentRemindersEnabled());
        response.setSystemAlertsEnabled(preference.getSystemAlertsEnabled());
        response.setPromotionsEnabled(preference.getPromotionsEnabled());
        return response;
    }
}
