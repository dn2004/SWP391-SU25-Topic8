package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;

@Component
public class StudentSpecification {

    public Specification<Student> hasFullName(String fullName) {
        return (root, query, criteriaBuilder) -> {
            if (fullName == null || fullName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%");
        };
    }

    public Specification<Student> hasClassGroup(ClassGroup classGroup) {
        return (root, query, criteriaBuilder) -> {
            if (classGroup == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("classGroup"), classGroup);
        };
    }

    public Specification<Student> hasClassValue(Class classValue) {
        return (root, query, criteriaBuilder) -> {
            if (classValue == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("classValue"), classValue);
        };
    }

    public Specification<Student> hasClassGroupIn(Collection<ClassGroup> classGroups) {
        return (root, query, criteriaBuilder) -> {
            if (classGroups == null || classGroups.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.get("classGroup").in(classGroups);
        };
    }

    public Specification<Student> hasClassValueIn(Collection<Class> classValues) {
        return (root, query, criteriaBuilder) -> {
            if (classValues == null || classValues.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.get("classValue").in(classValues);
        };
    }

    public Specification<Student> hasStatus(StudentStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public Specification<Student> dateOfBirthAfterOrEqual(LocalDate startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("dateOfBirth"), startDate);
        };
    }

    public Specification<Student> dateOfBirthBeforeOrEqual(LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("dateOfBirth"), endDate);
        };
    }

    public Specification<Student> dateOfBirthBetween(LocalDate startDate, LocalDate endDate) {
        return Specification.where(dateOfBirthAfterOrEqual(startDate))
                .and(dateOfBirthBeforeOrEqual(endDate));
    }

    public Specification<Student> hasDateOfBirth(LocalDate dateOfBirth) {
        return (root, query, criteriaBuilder) -> {
            if (dateOfBirth == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("dateOfBirth"), dateOfBirth);
        };
    }
}
