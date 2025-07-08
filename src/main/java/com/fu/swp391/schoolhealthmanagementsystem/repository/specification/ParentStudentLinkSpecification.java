package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.ParentStudentLink;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ParentStudentLinkSpecification {

    public Specification<ParentStudentLink> hasParent(User parent) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("parent"), parent);
    }

    public Specification<ParentStudentLink> studentHasStatus(StudentStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("student").get("status"), status);
    }
}

