package com.fu.swp391.schoolhealthmanagementsystem.repository.specification;

import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationCampaign;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.VaccinationCampaignStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class VaccinationCampaignSpecification {

    public Specification<VaccinationCampaign> hasName(String campaignName) {
        return (root, query, criteriaBuilder) -> {
            if (campaignName == null || campaignName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("campaignName")),
                    "%" + campaignName.toLowerCase() + "%"
            );
        };
    }

    public Specification<VaccinationCampaign> hasVaccineName(String vaccineName) {
        return (root, query, criteriaBuilder) -> {
            if (vaccineName == null || vaccineName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("vaccineName")),
                    "%" + vaccineName.toLowerCase() + "%"
            );
        };
    }

    public Specification<VaccinationCampaign> hasStatus(VaccinationCampaignStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public Specification<VaccinationCampaign> hasMultipleStatuses(List<VaccinationCampaignStatus> statuses) {
        return (root, query, criteriaBuilder) -> {
            if (statuses == null || statuses.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.get("status").in(statuses);
        };
    }

    public Specification<VaccinationCampaign> vaccinationDateAfterOrEqual(LocalDate startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("vaccinationDate"), startDate);
        };
    }

    public Specification<VaccinationCampaign> vaccinationDateBeforeOrEqual(LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("vaccinationDate"), endDate);
        };
    }

    public Specification<VaccinationCampaign> hasClassGroup(ClassGroup classGroup) {
        return (root, query, criteriaBuilder) -> {
            if (classGroup == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("targetClassGroup"), "%" + classGroup.name() + "%");
        };
    }

    public Specification<VaccinationCampaign> organizedBy(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("organizedByUser").get("userId"), userId);
        };
    }
}
