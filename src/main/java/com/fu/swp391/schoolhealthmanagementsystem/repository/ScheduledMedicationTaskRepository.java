package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.ScheduledMedicationTask;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedication;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledMedicationTaskRepository extends JpaRepository<ScheduledMedicationTask, Long>, JpaSpecificationExecutor<ScheduledMedicationTask> {
    // Hoặc dùng ID của StudentMedication
    Page<ScheduledMedicationTask> findByStudentMedication_StudentMedicationId(Long studentMedicationId, Pageable pageable);

    boolean existsByStudentMedicationAndScheduledDateAndScheduledTimeText(
            StudentMedication studentMedication, LocalDate scheduledDate, String scheduledTimeText);

    List<ScheduledMedicationTask> findByStudentMedicationAndScheduledDateGreaterThanEqualAndStatus(StudentMedication medication, LocalDate now, ScheduledMedicationTaskStatus scheduledMedicationTaskStatus);

    List<ScheduledMedicationTask> findByScheduledDateBeforeAndStatus(LocalDate today, ScheduledMedicationTaskStatus scheduledMedicationTaskStatus);


    @Query("SELECT COUNT(s) > 0 FROM ScheduledMedicationTask s WHERE s.studentMedication.studentMedicationId = :studentMedicationId AND s.status = com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus.ADMINISTERED")
    boolean existsAdministeredTasksByStudentMedicationId(@Param("studentMedicationId") Long studentMedicationId);

    long countByStatus(com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus status);
}
