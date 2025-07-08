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

    @Query("SELECT vc FROM VaccinationCampaign vc WHERE vc.status IN :statuses ORDER BY vc.vaccinationDate")
    List<VaccinationCampaign> findByStatusInOrderByVaccinationDate(@Param("statuses") List<VaccinationCampaignStatus> statuses);

    @Query("SELECT vc FROM VaccinationCampaign vc WHERE vc.status = :status AND vc.vaccinationDate = :date")
    List<VaccinationCampaign> findByStatusAndVaccinationDate(@Param("status") VaccinationCampaignStatus status, @Param("date") LocalDate date);

    @Query("SELECT vc FROM VaccinationCampaign vc WHERE vc.status = :status AND vc.consentDeadline = :date")
    List<VaccinationCampaign> findByStatusAndConsentDeadline(@Param("status") VaccinationCampaignStatus status, @Param("date") LocalDate date);

    @Query("SELECT vc FROM VaccinationCampaign vc WHERE vc.status = :status AND vc.vaccinationDate BETWEEN :startDate AND :endDate")
    List<VaccinationCampaign> findByStatusAndVaccinationDateBetween(
            @Param("status") VaccinationCampaignStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(sc) FROM VaccinationConsent sc WHERE sc.campaign.campaignId = :campaignId")
    Integer countConsentsForCampaign(@Param("campaignId") Long campaignId);

    @Query("SELECT COUNT(sc) FROM VaccinationConsent sc WHERE sc.campaign.campaignId = :campaignId AND sc.status = 'APPROVED'")
    Integer countApprovedConsentsForCampaign(@Param("campaignId") Long campaignId);

    @Query("SELECT COUNT(sc) FROM VaccinationConsent sc WHERE sc.campaign.campaignId = :campaignId AND sc.status = 'DECLINED'")
    Integer countDeclinedConsentsForCampaign(@Param("campaignId") Long campaignId);

    Page<VaccinationCampaign> findByStatus(VaccinationCampaignStatus status, Pageable pageable);

    Long countByStatus(VaccinationCampaignStatus status);
}
