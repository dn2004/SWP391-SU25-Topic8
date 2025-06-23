package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.ScheduledMedicationTask;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedication;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledMedicationTaskRepository extends JpaRepository<ScheduledMedicationTask, Long> {

    // Tìm các task theo StudentMedication, có phân trang
    Page<ScheduledMedicationTask> findByStudentMedication(StudentMedication studentMedication, Pageable pageable);

    // Hoặc dùng ID của StudentMedication
    Page<ScheduledMedicationTask> findByStudentMedication_StudentMedicationId(Long studentMedicationId, Pageable pageable);

    // Lấy tất cả task (không phân trang) của một StudentMedication, sắp xếp theo ngày và giờ
    @Query("SELECT smt FROM ScheduledMedicationTask smt " +
            "JOIN FETCH smt.studentMedication sm " +
            "JOIN FETCH sm.student s " +
            "WHERE smt.scheduledDate = :scheduledDate AND smt.status = :status")
    Page<ScheduledMedicationTask> findByScheduledDateAndStatusWithDetails(
            @Param("scheduledDate") LocalDate scheduledDate,
            @Param("status") ScheduledMedicationTaskStatus status,
            Pageable pageable
    );



    @Query("SELECT t FROM ScheduledMedicationTask t WHERE " +
            "t.administeredByStaff = :targetStaff " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:startDate IS NULL OR t.administeredAt >= :startDate) " +
            "AND (:endDate IS NULL OR t.administeredAt <= :endDate)")
    Page<ScheduledMedicationTask> findDynamicHandledTasksForStaff(
            @Param("targetStaff") User targetStaff,
            @Param("status") ScheduledMedicationTaskStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);




    boolean existsByStudentMedicationAndScheduledDateAndScheduledTimeText(
            StudentMedication studentMedication, LocalDate scheduledDate, String scheduledTimeText);

    List<ScheduledMedicationTask> findByStudentMedicationAndScheduledDateGreaterThanEqualAndStatus(StudentMedication medication, LocalDate now, ScheduledMedicationTaskStatus scheduledMedicationTaskStatus);

    List<ScheduledMedicationTask> findByScheduledDateBeforeAndStatus(LocalDate today, ScheduledMedicationTaskStatus scheduledMedicationTaskStatus);


    @Query("SELECT COUNT(s) > 0 FROM ScheduledMedicationTask s WHERE s.studentMedication.studentMedicationId = :studentMedicationId AND s.status = com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus.ADMINISTERED")
    boolean existsAdministeredTasksByStudentMedicationId(@Param("studentMedicationId") Long studentMedicationId);

    @Query("SELECT smt FROM ScheduledMedicationTask smt " +
            "JOIN FETCH smt.studentMedication sm " +
            "JOIN FETCH sm.student s " +
            "WHERE s.id = :studentId " +
            "AND smt.status <> com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus.SCHEDULED " +
            "AND (cast(:startDate as timestamp) IS NULL OR smt.administeredAt >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR smt.administeredAt <= :endDate)")
    Page<ScheduledMedicationTask> findStudentMedicationHistory(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT smt FROM ScheduledMedicationTask smt " +
            "JOIN FETCH smt.studentMedication sm " +
            "JOIN FETCH sm.student s " +
            "WHERE s.id = :studentId " +
            "AND smt.status = :status " +
            "AND (cast(:startDate as timestamp) IS NULL OR smt.administeredAt >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR smt.administeredAt <= :endDate)")
    Page<ScheduledMedicationTask> findStudentMedicationHistoryByStatus(
            @Param("studentId") Long studentId,
            @Param("status") ScheduledMedicationTaskStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT smt FROM ScheduledMedicationTask smt " +
            "JOIN FETCH smt.studentMedication sm " +
            "JOIN FETCH sm.student s " +
            "WHERE smt.status <> com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus.SCHEDULED " +
            "AND (:studentId IS NULL OR s.id = :studentId) " +
            "AND (:staffId IS NULL OR smt.administeredByStaff.userId = :staffId) " +
            "AND (:status IS NULL OR smt.status = :status) " +
            "AND (cast(:startDate as timestamp) IS NULL OR smt.administeredAt >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR smt.administeredAt <= :endDate)")
    Page<ScheduledMedicationTask> findAllHandledTasksHistory(
            @Param("studentId") Long studentId,
            @Param("staffId") Long staffId,
            @Param("status") ScheduledMedicationTaskStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
