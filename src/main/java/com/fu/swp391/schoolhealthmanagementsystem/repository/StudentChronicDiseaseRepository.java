        package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentChronicDisease;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudentChronicDiseaseRepository extends JpaRepository<StudentChronicDisease, Long>, JpaSpecificationExecutor<StudentChronicDisease> {

    Page<StudentChronicDisease> findByStudent(Student student, Pageable pageable);

    Page<StudentChronicDisease> findByStudent_Id(Long studentId, Pageable pageable);

}

