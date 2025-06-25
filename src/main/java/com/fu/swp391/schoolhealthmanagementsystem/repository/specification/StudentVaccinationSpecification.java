package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentVaccination;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class StudentVaccinationSpecification {

    public Specification<StudentVaccination> forStudent(Long studentId) {
        return (root, query, criteriaBuilder) -> {
            if (studentId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("student").get("id"), studentId);
        };
    }

    public Specification<StudentVaccination> hasStudentNameContaining(String studentName) {
        return (root, query, criteriaBuilder) -> {
            if (studentName == null || studentName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("fullName")), "%" + studentName.toLowerCase() + "%");
        };
    }

    public Specification<StudentVaccination> hasVaccineNameContaining(String vaccineName) {
        return (root, query, criteriaBuilder) -> {
            if (vaccineName == null || vaccineName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("vaccineName")), "%" + vaccineName.toLowerCase() + "%");
        };
    }

    public Specification<StudentVaccination> vaccinatedOnOrAfter(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("vaccinationDate"), date);
        };
    }

    public Specification<StudentVaccination> vaccinatedOnOrBefore(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("vaccinationDate"), date);
        };
    }

    public Specification<StudentVaccination> hasStatus(StudentVaccinationStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public Specification<StudentVaccination> approvedBy(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("approvedByUser").get("userId"), userId);
        };
    }
}
