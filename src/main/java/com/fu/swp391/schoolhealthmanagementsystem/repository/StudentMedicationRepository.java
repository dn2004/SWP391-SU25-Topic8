package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedication;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudentMedicationRepository extends JpaRepository<StudentMedication, Long> {
    List<StudentMedication> findAllByStatus(MedicationStatus status);

    @Query("SELECT sm FROM StudentMedication sm WHERE sm.status = :status AND sm.remainingDoses > 0 AND sm.scheduleStartDate IS NOT NULL AND sm.medicationTimeSlots IS NOT EMPTY AND (sm.nextScheduledTaskGenerationDate IS NULL OR sm.nextScheduledTaskGenerationDate <= :currentDate)")
    List<StudentMedication> findMedicationsRequiringTaskGeneration(@Param("status") MedicationStatus status, @Param("currentDate") LocalDate currentDate);

    Page<StudentMedication> findByStudent(Student student, Pageable pageable);

    Page<StudentMedication> findByStatus(MedicationStatus medicationStatus, Pageable pageable);

    @Query("SELECT sm FROM StudentMedication sm WHERE sm.student = :student AND " +
            "(:startDate IS NULL OR sm.dateReceived >= :startDate) AND " +
            "(:endDate IS NULL OR sm.dateReceived <= :endDate)")
    Page<StudentMedication> findByStudentAndDateRange(
            @Param("student") Student student,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT sm FROM StudentMedication sm WHERE " +
            "(:startDate IS NULL OR sm.dateReceived >= :startDate) AND " +
            "(:endDate IS NULL OR sm.dateReceived <= :endDate)")
    Page<StudentMedication> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT sm FROM StudentMedication sm WHERE sm.status = :status AND " +
            "(:startDate IS NULL OR sm.dateReceived >= :startDate) AND " +
            "(:endDate IS NULL OR sm.dateReceived <= :endDate)")
    Page<StudentMedication> findByStatusAndDateRange(
            @Param("status") MedicationStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT sm FROM StudentMedication sm JOIN FETCH sm.medicationTimeSlots WHERE sm.studentMedicationId = :id")
    Optional<StudentMedication> findWithMedicationTimeSlotsByStudentMedicationId(@Param("id") Long id);

    int countByStudent(Student student);
}