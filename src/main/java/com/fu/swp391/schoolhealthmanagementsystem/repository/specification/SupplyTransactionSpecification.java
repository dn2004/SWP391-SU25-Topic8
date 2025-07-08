package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.SupplyTransaction;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SupplyTransactionSpecification {

    public Specification<SupplyTransaction> forSupply(Long supplyId) {
        return (root, query, criteriaBuilder) -> {
            if (supplyId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("medicalSupply").get("supplyId"), supplyId);
        };
    }

    public Specification<SupplyTransaction> hasType(SupplyTransactionType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("supplyTransactionType"), type);
        };
    }

    public Specification<SupplyTransaction> performedByUser(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("performedByUser").get("userId"), userId);
        };
    }
}

