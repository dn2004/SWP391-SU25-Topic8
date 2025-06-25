package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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

    public Specification<Student> hasClassName(String className) {
        return (root, query, criteriaBuilder) -> {
            if (className == null || className.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("className")), "%" + className.toLowerCase() + "%");
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
}
