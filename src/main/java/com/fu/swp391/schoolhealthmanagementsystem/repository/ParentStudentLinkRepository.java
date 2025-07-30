package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.ParentStudentLink;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.LinkStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.RelationshipType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, Long>, JpaSpecificationExecutor<ParentStudentLink> {
    boolean existsByParentAndStudent(User parent, Student student);
    Optional<ParentStudentLink> findByParentAndStudent(User parent, Student student);

    boolean existsByParentAndStudentAndStatus(User currentUser, Student student, LinkStatus linkStatus);

    boolean existsByStudent(Student student);
    long countByStatus(LinkStatus status);

    @EntityGraph(attributePaths = {"parent", "student"})
    List<ParentStudentLink> findByStudent(Student student);
}