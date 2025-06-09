package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentVaccination;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentVaccinationRepository extends JpaRepository<StudentVaccination, Long> {
}