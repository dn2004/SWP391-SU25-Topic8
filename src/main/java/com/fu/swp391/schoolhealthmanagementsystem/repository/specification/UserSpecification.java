package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecification {

    public static Specification<User> withFullName(String fullName) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(fullName)) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + fullName.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), pattern);
        };
    }

    public static Specification<User> withEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(email)) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + email.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern);
        };
    }

    public static Specification<User> withPhone(String phone) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(phone)) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + phone + "%";
            return criteriaBuilder.like(root.get("phone"), pattern);
        };
    }

    public static Specification<User> withRole(UserRole role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("role"), role);
        };
    }

    public static Specification<User> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("isActive"), active);
        };
    }
}
