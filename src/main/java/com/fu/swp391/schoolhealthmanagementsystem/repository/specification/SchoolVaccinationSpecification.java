package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.SchoolVaccination;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolVaccinationStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SchoolVaccinationSpecification {

    public Specification<SchoolVaccination> forCampaign(Long campaignId) {
        return (root, query, criteriaBuilder) -> {
            if (campaignId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("campaign").get("campaignId"), campaignId);
        };
    }

    public Specification<SchoolVaccination> forStudent(Long studentId) {
        return (root, query, criteriaBuilder) -> {
            if (studentId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("student").get("id"), studentId);
        };
    }

    public Specification<SchoolVaccination> forStudentName(String studentName) {
        return (root, query, criteriaBuilder) -> {
            if (studentName == null || studentName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("student").get("fullName")),
                    "%" + studentName.toLowerCase() + "%"
            );
        };
    }

    public Specification<SchoolVaccination> forStudentClass(String className) {
        return (root, query, criteriaBuilder) -> {
            if (className == null || className.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("student").get("className")),
                    "%" + className.toLowerCase() + "%"
            );
        };
    }

    public Specification<SchoolVaccination> hasStatus(SchoolVaccinationStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public Specification<SchoolVaccination> vaccinationDateAfterOrEqual(LocalDate startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("vaccinationDate"), startDate);
        };
    }

    public Specification<SchoolVaccination> vaccinationDateBeforeOrEqual(LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("vaccinationDate"), endDate);
        };
    }

    public Specification<SchoolVaccination> administeredBy(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("administeredByUser").get("userId"), userId);
        };
    }
}
