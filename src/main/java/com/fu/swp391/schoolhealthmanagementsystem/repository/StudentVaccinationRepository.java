package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentVaccination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentVaccinationRepository extends JpaRepository<StudentVaccination, Long> {

    Page<StudentVaccination> findByStudent(Student student, Pageable pageable);

    Page<StudentVaccination> findByStudent_StudentId(Long studentId, Pageable pageable);
}