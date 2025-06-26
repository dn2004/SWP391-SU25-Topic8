package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.notification.NotificationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Notification;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.NotificationMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.NotificationRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Transactional
    public void createAndSendNotification(String recipientUsername, String content, String link) {
        User recipient = userRepository.findByEmail(recipientUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + recipientUsername));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .content(content)
                .link(link)
                .read(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Saved notification ID {} for user {}", savedNotification.getId(), recipientUsername);

        // Convert to DTO to send via WebSocket
        NotificationResponseDto notificationDto = notificationMapper.toDto(savedNotification);

        // Send real-time notification via WebSocket
        simpMessagingTemplate.convertAndSendToUser(recipientUsername, "/queue/notifications", notificationDto);
        log.info("Sent real-time notification to user {}", recipientUsername);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotificationsForUser(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipient_UserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByRecipient_UserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getRecipient().getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to access this notification.");
        }

        notification.setRead(true);
        Notification updatedNotification = notificationRepository.save(notification);
        return notificationMapper.toDto(updatedNotification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        log.info("Marked all notifications as read for user ID {}", userId);
    }

    @Transactional
    public void deleteOldNotifications() {
        LocalDateTime fifteenDaysAgo = LocalDateTime.now().minusDays(15);
        log.info("Deleting notifications created before {}", fifteenDaysAgo);
        notificationRepository.deleteByCreatedAtBefore(fifteenDaysAgo);
    }
}

