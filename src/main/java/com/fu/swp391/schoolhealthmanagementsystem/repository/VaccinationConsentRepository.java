package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationCampaign;
import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationConsent;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ConsentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaccinationConsentRepository extends JpaRepository<VaccinationConsent, Long>, JpaSpecificationExecutor<VaccinationConsent> {

    Optional<VaccinationConsent> findByCampaignAndStudent(VaccinationCampaign campaign, Student student);

    List<VaccinationConsent> findByCampaign(VaccinationCampaign campaign);

    Page<VaccinationConsent> findByCampaign(VaccinationCampaign campaign, Pageable pageable);

    List<VaccinationConsent> findByCampaignAndStatus(VaccinationCampaign campaign, ConsentStatus status);

    Page<VaccinationConsent> findByCampaignAndStatus(VaccinationCampaign campaign, ConsentStatus status, Pageable pageable);

    List<VaccinationConsent> findByStudent(Student student);

    Page<VaccinationConsent> findByStudent(Student student, Pageable pageable);

    List<VaccinationConsent> findByParent(User parent);

    Page<VaccinationConsent> findByParent(User parent, Pageable pageable);

    @Query("SELECT vc FROM VaccinationConsent vc WHERE vc.campaign.campaignId = :campaignId AND vc.status = 'APPROVED'")
    List<VaccinationConsent> findApprovedConsentsByCampaignId(@Param("campaignId") Long campaignId);

    @Query("SELECT COUNT(vc) FROM VaccinationConsent vc WHERE vc.campaign.campaignId = :campaignId AND vc.status = :status")
    Integer countByCampaignIdAndStatus(@Param("campaignId") Long campaignId, @Param("status") ConsentStatus status);

    @Query("SELECT vc FROM VaccinationConsent vc " +
           "WHERE vc.campaign.campaignId = :campaignId " +
           "AND vc.status = 'PENDING' " +
           "AND vc.reminderSentAt IS NULL")
    List<VaccinationConsent> findPendingConsentsWithNoReminder(@Param("campaignId") Long campaignId);

    List<VaccinationConsent> findByStatus(ConsentStatus consentStatus);
}
