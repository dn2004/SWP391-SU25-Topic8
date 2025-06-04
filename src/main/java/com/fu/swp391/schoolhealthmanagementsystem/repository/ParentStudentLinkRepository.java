package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.ParentStudentLink;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, Long> {
    boolean existsByParentAndStudent(User parent, Student student);
    Optional<ParentStudentLink> findByParentAndStudent(User parent, Student student);
}