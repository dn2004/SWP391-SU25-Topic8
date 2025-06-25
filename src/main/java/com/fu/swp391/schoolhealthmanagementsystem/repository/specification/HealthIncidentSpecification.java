package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.HealthIncident;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.HealthIncidentType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class HealthIncidentSpecification {

    public Specification<HealthIncident> forStudent(Long studentId) {
        return (root, query, criteriaBuilder) -> {
            if (studentId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("student").get("id"), studentId);
        };
    }

    public Specification<HealthIncident> forStudentName(String studentName) {
        return (root, query, criteriaBuilder) -> {
            if (studentName == null || studentName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("student").get("fullName")), "%" + studentName.toLowerCase() + "%");
        };
    }

    public Specification<HealthIncident> recordedBy(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("recordedByUser").get("userId"), userId);
        };
    }

    public Specification<HealthIncident> recordedByName(String userName) {
        return (root, query, criteriaBuilder) -> {
            if (userName == null || userName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("recordedByUser").get("fullName")), "%" + userName.toLowerCase() + "%");
        };
    }

    public Specification<HealthIncident> happenedOnOrAfter(LocalDateTime startDateTime) {
        return (root, query, criteriaBuilder) -> {
            if (startDateTime == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("incidentDateTime"), startDateTime);
        };
    }

    public Specification<HealthIncident> happenedOnOrBefore(LocalDateTime endDateTime) {
        return (root, query, criteriaBuilder) -> {
            if (endDateTime == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("incidentDateTime"), endDateTime);
        };
    }

    public Specification<HealthIncident> hasType(HealthIncidentType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("incidentType"), type);
        };
    }

    public Specification<HealthIncident> hasLocationContaining(String location) {
        return (root, query, criteriaBuilder) -> {
            if (location == null || location.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), "%" + location.toLowerCase() + "%");
        };
    }

    public Specification<HealthIncident> descriptionContaining(String text) {
        return (root, query, criteriaBuilder) -> {
            if (text == null || text.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + text.toLowerCase() + "%");
        };
    }

    public Specification<HealthIncident> isNotDeleted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted"));
    }
}
