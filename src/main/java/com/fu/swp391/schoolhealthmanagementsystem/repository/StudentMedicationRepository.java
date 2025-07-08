package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedication;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudentMedicationRepository extends JpaRepository<StudentMedication, Long>, JpaSpecificationExecutor<StudentMedication> {
    List<StudentMedication> findAllByStatus(MedicationStatus status);

    Page<StudentMedication> findByStudent(Student student, Pageable pageable);

    Page<StudentMedication> findByStatus(MedicationStatus medicationStatus, Pageable pageable);

    // Tên phương thức giờ đây chỉ cần mô tả việc tìm kiếm,
// việc fetch được khai báo qua annotation.
    @EntityGraph(attributePaths = {"medicationTimeSlots"})
    Optional<StudentMedication> findByStudentMedicationId(Long id);

    int countByStudent(Student student);

    long countByStatus(MedicationStatus status);
}