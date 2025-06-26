package com.fu.swp391.schoolhealthmanagementsystem.scheduler;

import com.fu.swp391.schoolhealthmanagementsystem.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupScheduler {

    private final NotificationService notificationService;

    /**
     * Chạy tác vụ vào lúc 1:00 AM mỗi ngày để dọn dẹp thông báo cũ.
     * Cron expression: second, minute, hour, day of month, month, day(s) of week
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanupOldNotifications() {
        log.info("Bắt đầu tác vụ định kỳ dọn dẹp thông báo cũ.");
        try {
            notificationService.deleteOldNotifications();
            log.info("Hoàn thành tác vụ định kỳ dọn dẹp thông báo cũ.");
        } catch (Exception e) {
            log.error("Lỗi trong quá trình chạy tác vụ dọn dẹp thông báo cũ", e);
        }
    }
}

