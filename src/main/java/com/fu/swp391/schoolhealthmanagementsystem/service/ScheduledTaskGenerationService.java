package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicationTimeSlot;
import com.fu.swp391.schoolhealthmanagementsystem.entity.ScheduledMedicationTask;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedication;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User; // Cần nếu task có createdBy
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicationStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import com.fu.swp391.schoolhealthmanagementsystem.repository.ScheduledMedicationTaskRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentMedicationRepository;
import com.fu.swp391.schoolhealthmanagementsystem.util.DateUtils; // Giả sử có class này
import com.fu.swp391.schoolhealthmanagementsystem.util.SchoolSessionUtil; // Giả sử có class này
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskGenerationService {

    private final StudentMedicationRepository studentMedicationRepository;
    private final ScheduledMedicationTaskRepository scheduledTaskRepository;
    // Nên có một User hệ thống để gán cho createdBy của task nếu không có user cụ thể
    // private final UserRepository userRepository;


    @Transactional
    public void generateScheduledTasks(Long studentMedicationId) {
        log.info("Starting scheduled task generation process...");
        List<StudentMedication> medicationsToSchedule;

        if (studentMedicationId != null) {
            StudentMedication sm = studentMedicationRepository.findById(studentMedicationId)
                    .orElse(null);
            medicationsToSchedule = (sm != null) ? List.of(sm) : List.of();
        } else {
            // Lấy tất cả các StudentMedication cần được lên lịch
            // (active, còn liều, có thông tin lịch, và next generation date phù hợp)
            medicationsToSchedule = studentMedicationRepository
                    .findAllByStatus(MedicationStatus.AVAILABLE);
            // Có thể thêm điều kiện lọc theo NextScheduledTaskGenerationDate
        }

        if (medicationsToSchedule.isEmpty()) {
            log.info("No student medications found requiring task generation at this time.");
            return;
        }

        for (StudentMedication medication : medicationsToSchedule) {
            processMedicationSchedule(medication);
        }
        log.info("Finished scheduled task generation process.");
    }

    // Trong ScheduledTaskGenerationService.java

    private void processMedicationSchedule(StudentMedication medication) {
        // Điều kiện kiểm tra ban đầu: status phải là AVAILABLE, còn liều, có ngày bắt đầu và có các cữ uống
        log.info("Processing schedule for StudentMedication ID: {}, and remaining dose: {}", medication.getStudentMedicationId(), medication.getRemainingDoses());
        if (medication.getStatus() != MedicationStatus.AVAILABLE ||
                medication.getRemainingDoses() == null || medication.getRemainingDoses() <= 0 ||
                medication.getScheduleStartDate() == null ||
                medication.getMedicationTimeSlots() == null || medication.getMedicationTimeSlots().isEmpty()) {

            if (medication.getStatus() == MedicationStatus.AVAILABLE &&
                    medication.getRemainingDoses() != null && medication.getRemainingDoses() <= 0) {
                medication.setStatus(MedicationStatus.OUT_OF_DOSES);
                log.info("StudentMedication ID {} status will be updated to OUT_OF_DOSES as remaining doses hit zero.", medication.getStudentMedicationId());
            } else {
                log.warn("Skipping task generation for StudentMedication ID {}: Not AVAILABLE, no remaining doses, or missing schedule info. Current status: {}",
                        medication.getStudentMedicationId(), medication.getStatus());
            }
            return; // Không tạo task nếu điều kiện không đủ
        }

        LocalDate today = LocalDate.now();

        // [SỬA 1] Hoàn thiện logic kiểm tra ngày làm việc
        if (!DateUtils.isWorkday(today)) {
            log.info("Skipping task generation for StudentMedication ID {}: Today ({}) is not a workday.", medication.getStudentMedicationId(), today);
            return; // Thoát nếu là cuối tuần
        }

        // Nếu ngày bắt đầu lịch trình là trong tương lai, không làm gì cả
        if (medication.getScheduleStartDate().isAfter(today)) {
            log.info("Skipping task generation for StudentMedication ID {}: Schedule start date {} is in the future.", medication.getStudentMedicationId(), medication.getScheduleStartDate());
            return;
        }

        List<ScheduledMedicationTask> newTasks = new ArrayList<>();
        int currentRemainingDoses = medication.getRemainingDoses();
        int predictDoes;
        List<MedicationTimeSlot> timeSlots = medication.getMedicationTimeSlots();

        // Với mỗi cữ uống được định nghĩa cho medication này
        for (MedicationTimeSlot slot : timeSlots) {
            predictDoes = currentRemainingDoses - 1;
            if (predictDoes < 0) {
                log.warn("No more doses left for StudentMedication ID {}. Skipping task generation for time slot: {}", medication.getStudentMedicationId(), slot.getTimeExpression());
                continue; // Không tạo task nếu không còn liều
            }

            // Kiểm tra xem task cho ngày này, thời điểm này đã tồn tại chưa
            if (!scheduledTaskRepository.existsByStudentMedicationAndScheduledDateAndScheduledTimeText(
                    medication, today, slot.getTimeExpression())) {

                ScheduledMedicationTask newTask = ScheduledMedicationTask.builder()
                        .studentMedication(medication)
                        .scheduledDate(today)
                        .scheduledTimeText(slot.getTimeExpression())
                        .status(ScheduledMedicationTaskStatus.SCHEDULED)
                        .schoolSession(slot.getSchoolSessionHint() != null ? slot.getSchoolSessionHint() : SchoolSessionUtil.getSessionFromTime(slot.getTimeExpression()))
                        .requestedAt(LocalDateTime.now())
                        .build();
                newTasks.add(newTask);
                log.info("Generated new scheduled task for StudentMedication ID {}: {}", medication.getStudentMedicationId(), newTask);
            }
        }

        if (!newTasks.isEmpty()) {
            scheduledTaskRepository.saveAll(newTasks);
            log.info("Generated {} new scheduled tasks for StudentMedication ID {}.", newTasks.size(), medication.getStudentMedicationId());
        }

        if (currentRemainingDoses <= 0) {
            medication.setStatus(MedicationStatus.OUT_OF_DOSES);
            log.info("StudentMedication ID {} status will be updated to OUT_OF_DOSES. Final remaining doses: {}.",
                    medication.getStudentMedicationId(), currentRemainingDoses);
        }
    }
}