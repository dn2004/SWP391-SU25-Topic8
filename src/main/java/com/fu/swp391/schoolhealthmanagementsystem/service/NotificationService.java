package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.notification.NotificationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Notification;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final AuthorizationService authorizationService;

    private static final String SYSTEM_SENDER = "system";

    @Transactional
    public void createAndSendNotification(String recipientUsername, String content, String link, String senderUsername) {
        User recipient = userRepository.findByEmail(recipientUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + recipientUsername));

        User sender = null;
        if (senderUsername != null && !senderUsername.equalsIgnoreCase(SYSTEM_SENDER)) {
            sender = userRepository.findByEmail(senderUsername)
                    .orElse(null); // Không ném lỗi nếu không tìm thấy người gửi, chỉ log
            if (sender == null) {
                log.warn("Không tìm thấy người gửi với username '{}'. Thông báo sẽ được gửi dưới tên hệ thống.", senderUsername);
            }
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender) // Gán người gửi
                .content(content)
                .link(link)
                .read(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Đã lưu thông báo ID {} cho người dùng {} từ người gửi {}", savedNotification.getId(), recipientUsername, senderUsername);

        // Chuyển đổi sang DTO để gửi qua WebSocket
        NotificationResponseDto notificationDto = notificationMapper.toDto(savedNotification);

        // Gửi thông báo realtime qua WebSocket
        simpMessagingTemplate.convertAndSendToUser(recipientUsername, "/queue/notifications", notificationDto);
        log.info("Đã gửi thông báo realtime tới người dùng {}", recipientUsername);
    }

    @Transactional
    public void createAndSendNotifications(List<User> recipients, String content, String link, String senderUsername) {
        if (recipients == null || recipients.isEmpty()) {
            return;
        }
        for (User recipient : recipients) {
            if (recipient != null && recipient.getEmail() != null) {
                createAndSendNotification(recipient.getEmail(), content, link, senderUsername);
            }
        }
    }

    @Transactional
    public void createAndSendNotificationToRole(UserRole role, String content, String link, String senderUsername) {
        List<User> usersInRole = userRepository.findAllByRole(role);
        createAndSendNotifications(usersInRole, content, link, senderUsername);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotificationsForUser(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipient_UserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByRecipient_UserIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo với ID: " + notificationId));

        if (!notification.getRecipient().getUserId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập thông báo này.");
        }

        notification.setRead(true);
        Notification updatedNotification = notificationRepository.save(notification);
        log.info("Đã đánh dấu thông báo ID {} là đã đọc cho user ID {}", notificationId, userId);
        return notificationMapper.toDto(updatedNotification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        log.info("Đã đánh dấu tất cả thông báo là đã đọc cho user ID {}", userId);
    }

    @Transactional
    public void deleteOldNotifications() {
        LocalDateTime fifteenDaysAgo = LocalDateTime.now().minusDays(15);
        log.info("Đang xóa các thông báo được tạo trước ngày {}", fifteenDaysAgo);
        notificationRepository.deleteByCreatedAtBefore(fifteenDaysAgo);
    }
}
