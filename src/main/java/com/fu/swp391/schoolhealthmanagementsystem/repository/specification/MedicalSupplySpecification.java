package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicalSupply;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicalSupplyStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MedicalSupplySpecification {

    public Specification<MedicalSupply> hasNameContaining(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public Specification<MedicalSupply> hasCategory(String category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null || category.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("category"), category);
        };
    }

    public Specification<MedicalSupply> hasStatus(MedicalSupplyStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public Specification<MedicalSupply> isExpired(boolean expired) {
        return (root, query, criteriaBuilder) -> {
            if (expired) {
                return criteriaBuilder.lessThan(root.get("expiredDate"), LocalDate.now());
            } else {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("expiredDate"), LocalDate.now());
            }
        };
    }

    public Specification<MedicalSupply> stockLessThan(int quantity) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.lessThan(root.get("currentStock"), quantity);
    }

    public Specification<MedicalSupply> stockGreaterThan(int quantity) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.greaterThan(root.get("currentStock"), quantity);
    }

    public Specification<MedicalSupply> hasExpiredDateFrom(LocalDate from) {
        return (root, query, criteriaBuilder) -> {
            if (from == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("expiredDate"), from);
        };
    }

    public Specification<MedicalSupply> hasExpiredDateTo(LocalDate to) {
        return (root, query, criteriaBuilder) -> {
            if (to == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("expiredDate"), to);
        };
    }
}
