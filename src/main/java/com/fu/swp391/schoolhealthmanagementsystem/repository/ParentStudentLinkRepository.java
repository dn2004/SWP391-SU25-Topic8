package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.ParentStudentLink;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.LinkStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, Long>, JpaSpecificationExecutor<ParentStudentLink> {
    boolean existsByParentAndStudent(User parent, Student student);
    Optional<ParentStudentLink> findByParentAndStudent(User parent, Student student);

    @Query("SELECT psl.student FROM ParentStudentLink psl WHERE psl.parent = :parent")
    Page<Student> findStudentsByParent(User parent, Pageable pageable);

    boolean existsByParentAndStudentAndStatus(User currentUser, Student student, LinkStatus linkStatus);

    boolean existsByStudent(Student student);
}