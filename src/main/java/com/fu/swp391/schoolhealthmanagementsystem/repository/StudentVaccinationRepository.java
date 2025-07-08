package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentVaccination;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudentVaccinationRepository extends JpaRepository<StudentVaccination, Long>, JpaSpecificationExecutor<StudentVaccination> {

    Page<StudentVaccination> findByStatus(StudentVaccinationStatus status, Pageable pageable);

    Page<StudentVaccination> findByStudent_IdAndStatus(Long studentId, StudentVaccinationStatus status, Pageable pageable);

    long countByStatus(com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus status);
}