package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.entity.ScheduledMedicationTask;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import com.fu.swp391.schoolhealthmanagementsystem.repository.ScheduledMedicationTaskRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskCleanupService {

    private final ScheduledMedicationTaskRepository taskRepository;
    private final UserRepository userRepository; // Để lấy User hệ thống (tùy chọn)

    @Transactional
    public void markOverdueTasksAsSkipped() {
        LocalDate today = LocalDate.now();
        log.info("Running job to mark overdue scheduled medication tasks as SKIPPED_SYSTEM_OVERDUE for dates before {}.", today);

        List<ScheduledMedicationTask> overdueTasks = taskRepository.findByScheduledDateBeforeAndStatus(
                today,
                ScheduledMedicationTaskStatus.SCHEDULED
        );

        if (overdueTasks.isEmpty()) {
            log.info("No overdue scheduled tasks found to mark as skipped.");
            return;
        }

        User systemUser = findSystemUser(); // Hàm helper để lấy một user hệ thống (nếu có)

        int count = 0;
        for (ScheduledMedicationTask task : overdueTasks) {
            task.setStatus(ScheduledMedicationTaskStatus.SKIPPED_SYSTEM_OVERDUE);
            task.setStaffNotes((task.getStaffNotes() == null ? "" : task.getStaffNotes() + "\n") +
                    "Tự động bỏ qua do quá hạn xử lý vào ngày " + LocalDateTime.now().toLocalDate());
            task.setAdministeredAt(LocalDateTime.now()); // Thời điểm hệ thống ghi nhận việc skip
            if (systemUser != null) {
                task.setAdministeredByStaff(systemUser); // Gán cho user hệ thống nếu có
            }
            // Không thay đổi StudentMedication.remainingDoses
            // Không dời lịch tự động
            count++;
        }

        taskRepository.saveAll(overdueTasks); // Lưu tất cả các task đã cập nhật
        log.info("Successfully marked {} overdue scheduled tasks as SKIPPED_SYSTEM_OVERDUE.", count);
    }

    private User findSystemUser() {
        // Logic để tìm một user hệ thống, ví dụ:
        // return userRepository.findByEmail("system@schoolhealth.com").orElse(null);
        // Hoặc trả về null nếu không có user hệ thống riêng.
        // Nếu trả về null, trường administeredByStaff của task sẽ là null.
        // Trong thực tế, bạn nên có một tài khoản hệ thống cho các tác vụ tự động.
        return userRepository.findAll().stream().findFirst().orElse(null); // Ví dụ rất đơn giản, không nên dùng trong production
    }

}
