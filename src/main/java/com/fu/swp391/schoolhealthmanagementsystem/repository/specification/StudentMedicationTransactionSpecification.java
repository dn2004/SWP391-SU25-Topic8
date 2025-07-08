package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedication;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedicationTransaction;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentMedicationTransactionType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;

@Component
public class StudentMedicationTransactionSpecification {

    public Specification<StudentMedicationTransaction> belongsToMedicationId(Long medicationId) {
        return (root, query, criteriaBuilder) -> {
            if (medicationId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("studentMedication").get("studentMedicationId"), medicationId);
        };
    }

    public Specification<StudentMedicationTransaction> hasTransactionType(StudentMedicationTransactionType transactionType) {
        return (root, query, criteriaBuilder) -> {
            if (transactionType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("transactionType"), transactionType);
        };
    }

    public Specification<StudentMedicationTransaction> createdOnOrAfter(LocalDateTime startDateTime) {
        return (root, query, criteriaBuilder) -> {
            if (startDateTime == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDateTime"), startDateTime);
        };
    }

    public Specification<StudentMedicationTransaction> createdOnOrBefore(LocalDateTime endDateTime) {
        return (root, query, criteriaBuilder) -> {
            if (endDateTime == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("transactionDateTime"), endDateTime);
        };
    }

    public Specification<StudentMedicationTransaction> performedByUser(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("performedByUser").get("userId"), userId);
        };
    }

    public Specification<StudentMedicationTransaction> relatedToScheduledTask(Long taskId) {
        return (root, query, criteriaBuilder) -> {
            if (taskId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("scheduledMedicationTask").get("taskId"), taskId);
        };
    }


    public Specification<StudentMedicationTransaction> notesContaining(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (searchText == null || searchText.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("notes")),
                "%" + searchText.toLowerCase() + "%"
            );
        };
    }
}
