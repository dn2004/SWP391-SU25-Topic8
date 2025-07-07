package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationCampaign;
import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationConsent;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class VaccinationConsentSpecification {

    public Specification<VaccinationConsent> forCampaign(VaccinationCampaign campaign) {
        return (root, query, criteriaBuilder) -> {
            if (campaign == null) {
                // Campaign is a mandatory filter, so we can return a predicate that is always false if it's null,
                // or handle it in the service layer. Returning conjunction for now.
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("campaign"), campaign);
        };
    }

    public Specification<VaccinationConsent> hasStudentNameContaining(String studentName) {
        return (root, query, criteriaBuilder) -> {
            if (studentName == null || studentName.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            // Path: VaccinationConsent -> Student -> User -> fullName
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("student").get("user").get("fullName")),
                "%" + studentName.trim().toLowerCase() + "%"
            );
        };
    }

    public Specification<VaccinationConsent> hasClassNameContaining(String className) {
        return (root, query, criteriaBuilder) -> {
            if (className == null || className.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            // Path: VaccinationConsent -> Student -> className
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("student").get("className")),
                "%" + className.trim().toLowerCase() + "%"
            );
        };
    }
}

