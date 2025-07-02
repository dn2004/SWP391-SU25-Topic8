package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentChronicDisease;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentChronicDiseaseStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class StudentChronicDiseaseSpecification {

    public Specification<StudentChronicDisease> forStudent(Long studentId) {
        return (root, query, cb) -> {
            if (studentId == null) {
                return cb.isTrue(cb.literal(true)); // always true = no filtering
            }
            return cb.equal(root.get("student").get("id"), studentId);
        };
    }

    public Specification<StudentChronicDisease> hasStudentNameContaining(String studentName) {
        return (root, query, cb) -> {
            if (studentName == null || studentName.trim().isEmpty()) {
                return cb.isTrue(cb.literal(true));
            }
            Join<StudentChronicDisease, Student> studentJoin = root.join("student");
            return cb.like(cb.lower(studentJoin.get("fullName")), "%" + studentName.toLowerCase() + "%");
        };
    }

    public Specification<StudentChronicDisease> hasDiseaseNameContaining(String diseaseName) {
        return (root, query, cb) -> {
            if (diseaseName == null || diseaseName.trim().isEmpty()) {
                return cb.isTrue(cb.literal(true));
            }
            return cb.like(cb.lower(root.get("diseaseName")), "%" + diseaseName.toLowerCase() + "%");
        };
    }

    public Specification<StudentChronicDisease> diagnosedOnOrAfter(LocalDate fromDate) {
        return (root, query, cb) -> {
            if (fromDate == null) {
                return cb.isTrue(cb.literal(true));
            }
            return cb.greaterThanOrEqualTo(root.get("diagnosedDate"), fromDate);
        };
    }

    public Specification<StudentChronicDisease> diagnosedOnOrBefore(LocalDate toDate) {
        return (root, query, cb) -> {
            if (toDate == null) {
                return cb.isTrue(cb.literal(true));
            }
            return cb.lessThanOrEqualTo(root.get("diagnosedDate"), toDate);
        };
    }

    public Specification<StudentChronicDisease> hasStatus(StudentChronicDiseaseStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.isTrue(cb.literal(true));
            }
            return cb.equal(root.get("status"), status);
        };
    }
}

