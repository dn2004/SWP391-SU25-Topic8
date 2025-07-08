package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedication;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicationStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class StudentMedicationSpecification {

    public Specification<StudentMedication> hasStatus(MedicationStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public Specification<StudentMedication> receivedOnOrAfter(LocalDate startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("dateReceived"), startDate);
        };
    }

    public Specification<StudentMedication> receivedOnOrBefore(LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("dateReceived"), endDate);
        };
    }


    public Specification<StudentMedication> hasStudent(Student student) {
        return (root, query, criteriaBuilder) -> {
            if (student == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("student"), student);
        };
    }
}
