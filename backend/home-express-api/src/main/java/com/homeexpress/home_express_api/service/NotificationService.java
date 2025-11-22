package com.homeexpress.home_express_api.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homeexpress.home_express_api.dto.response.NotificationResponse;
import com.homeexpress.home_express_api.entity.Notification;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.repository.NotificationRepository;
import com.homeexpress.home_express_api.repository.UserRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationPreferenceService preferenceService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            NotificationPreferenceService preferenceService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.preferenceService = preferenceService;
    }

    @Transactional
    public NotificationResponse createNotification(
            Long userId,
            Notification.NotificationType type,
            String title,
            String message,
            Notification.ReferenceType referenceType,
            Long referenceId,
            Notification.Priority priority) {

        if (!preferenceService.shouldSendNotification(userId, type)) {
            return null;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setPriority(priority != null ? priority : Notification.Priority.MEDIUM);
        notification.setIsRead(false);

        notification = notificationRepository.save(notification);
        return mapToResponse(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Boolean isRead, Pageable pageable) {
        Page<Notification> notifications;

        if (isRead != null) {
            notifications = notificationRepository.findByUser_UserIdAndIsRead(userId, isRead, pageable);
        } else {
            notifications = notificationRepository.findByUser_UserId(userId, pageable);
        }

        return notifications.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponse(notification);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return mapToResponse(notification);
    }

    @Transactional
    public void markMultipleAsRead(List<Long> notificationIds, Long userId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }

        notificationRepository.markAsReadByIds(notificationIds, userId, LocalDateTime.now());
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        notificationRepository.delete(notification);
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public void cleanupOldNotifications(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        notificationRepository.deleteOldNotifications(cutoffDate);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setNotificationId(notification.getNotificationId());
        response.setType(notification.getType().name());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setReferenceType(notification.getReferenceType() != null ? notification.getReferenceType().name() : null);
        response.setReferenceId(notification.getReferenceId());
        response.setActionUrl(buildActionUrl(notification));
        response.setIsRead(notification.getIsRead());
        response.setReadAt(notification.getReadAt());
        response.setPriority(notification.getPriority().name());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }

    private String buildActionUrl(Notification notification) {
        if (notification.getReferenceType() == null || notification.getReferenceId() == null) {
            return null;
        }

        String userRole = notification.getUser().getRole().name().toLowerCase();

        switch (notification.getReferenceType()) {
            case BOOKING:
                return "/" + userRole + "/bookings/" + notification.getReferenceId();
            case QUOTATION:
                return "/" + userRole + "/quotations/" + notification.getReferenceId();
            case CONTRACT:
                return "/" + userRole + "/contracts/" + notification.getReferenceId();
            case PAYMENT:
                return "/" + userRole + "/payments/" + notification.getReferenceId();
            case VEHICLE:
                return "/" + userRole + "/vehicles/" + notification.getReferenceId();
            case CUSTOMER:
                return "/" + userRole + "/customers/" + notification.getReferenceId();
            case TRANSPORT:
                return "/" + userRole + "/transports/" + notification.getReferenceId();
            default:
                return null;
        }
    }
}
