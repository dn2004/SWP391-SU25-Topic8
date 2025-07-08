package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationCampaign;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.VaccinationCampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VaccinationCampaignRepository extends JpaRepository<VaccinationCampaign, Long>, JpaSpecificationExecutor<VaccinationCampaign> {

    List<VaccinationCampaign> findByStatusAndVaccinationDate(VaccinationCampaignStatus status, LocalDate date);

    List<VaccinationCampaign> findByStatusAndConsentDeadline(VaccinationCampaignStatus status, LocalDate date);


    @Query("SELECT COUNT(sc) FROM VaccinationConsent sc WHERE sc.campaign.campaignId = :campaignId")
    Integer countConsentsForCampaign(@Param("campaignId") Long campaignId);

    @Query("SELECT COUNT(sc) FROM VaccinationConsent sc WHERE sc.campaign.campaignId = :campaignId AND sc.status = 'APPROVED'")
    Integer countApprovedConsentsForCampaign(@Param("campaignId") Long campaignId);

    @Query("SELECT COUNT(sc) FROM VaccinationConsent sc WHERE sc.campaign.campaignId = :campaignId AND sc.status = 'DECLINED'")
    Integer countDeclinedConsentsForCampaign(@Param("campaignId") Long campaignId);

    Long countByStatus(VaccinationCampaignStatus status);
}
