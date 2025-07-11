package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Blog;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class StudentSpecification {

    public Specification<Student> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // Thêm điều kiện tìm kiếm theo fullName
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), likePattern));

            // Thử chuyển đổi search thành Long để tìm theo ID
            try {
                Long id = Long.parseLong(search);
                predicates.add(criteriaBuilder.equal(root.get("id"), id));
            } catch (NumberFormatException e) {
                // Bỏ qua nếu không phải là số
            }

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<Student> hasClassGroup(ClassGroup classGroup) {
        return (root, query, criteriaBuilder) ->
                classGroup == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("classGroup"), classGroup);
    }

    public Specification<Student> hasClassValue(Class classValue) {
        return (root, query, criteriaBuilder) ->
                classValue == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("classValue"), classValue);
    }

    public Specification<Student> hasStatus(StudentStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("status"), status);
    }

    // StudentSpecification.java
    public Specification<Student> hasGender(Gender gender) {
        return (root, query, criteriaBuilder) -> {
            if (gender == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("gender"), gender);
        };
    }

    public Specification<Student> hasDateOfBirth(LocalDate dateOfBirth) {
        return (root, query, criteriaBuilder) ->
                dateOfBirth == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("dateOfBirth"), dateOfBirth);
    }
}
