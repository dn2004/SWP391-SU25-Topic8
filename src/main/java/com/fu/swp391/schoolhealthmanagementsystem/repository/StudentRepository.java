package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentCode(String studentCode); // Existing
    Optional<Student> findByInvitationCode(String invitationCode); // New
}