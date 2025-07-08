package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.ScheduledMedicationTask;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolSession;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

@Component
public class ScheduledMedicationTaskSpecification {

    public Specification<ScheduledMedicationTask> hasScheduledDate(LocalDate scheduledDate) {
        return (root, query, criteriaBuilder) -> {
            if (scheduledDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("scheduledDate"), scheduledDate);
        };
    }

    public Specification<ScheduledMedicationTask> hasStatus(ScheduledMedicationTaskStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public Specification<ScheduledMedicationTask> administeredOnOrAfter(LocalDateTime startDateTime) {
        return (root, query, criteriaBuilder) -> {
            if (startDateTime == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("administeredAt"), startDateTime);
        };
    }

    public Specification<ScheduledMedicationTask> administeredOnOrBefore(LocalDateTime endDateTime) {
        return (root, query, criteriaBuilder) -> {
            if (endDateTime == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("administeredAt"), endDateTime);
        };
    }

    public Specification<ScheduledMedicationTask> administeredByStaff(Long staffId) {
        return (root, query, criteriaBuilder) -> {
            if (staffId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("administeredByStaff").get("userId"), staffId);
        };
    }

    public Specification<ScheduledMedicationTask> forStudent(Long studentId) {
        return (root, query, criteriaBuilder) -> {
            if (studentId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("studentMedication").get("student").get("id"), studentId);
        };
    }

    public Specification<ScheduledMedicationTask> isHandled() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.notEqual(root.get("status"), ScheduledMedicationTaskStatus.SCHEDULED);
    }
}
