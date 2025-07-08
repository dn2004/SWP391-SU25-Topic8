package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.SchoolVaccination;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationCampaign;
import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationConsent;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolVaccinationStatus;
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
public interface SchoolVaccinationRepository extends JpaRepository<SchoolVaccination, Long>, JpaSpecificationExecutor<SchoolVaccination> {

    Optional<SchoolVaccination> findByConsent(VaccinationConsent consent);

    List<SchoolVaccination> findByCampaign(VaccinationCampaign campaign);

    Page<SchoolVaccination> findByCampaign(VaccinationCampaign campaign, Pageable pageable);

    List<SchoolVaccination> findByCampaignAndStatus(VaccinationCampaign campaign, SchoolVaccinationStatus status);

    Page<SchoolVaccination> findByCampaignAndStatus(VaccinationCampaign campaign, SchoolVaccinationStatus status, Pageable pageable);

    List<SchoolVaccination> findByStudent(Student student);

    Page<SchoolVaccination> findByStudent(Student student, Pageable pageable);

    @Query("SELECT sv FROM SchoolVaccination sv WHERE sv.campaign.campaignId = :campaignId AND sv.status = :status")
    List<SchoolVaccination> findByCampaignIdAndStatus(@Param("campaignId") Long campaignId, @Param("status") SchoolVaccinationStatus status);

    @Query("SELECT COUNT(sv) FROM SchoolVaccination sv WHERE sv.campaign.campaignId = :campaignId AND sv.status = :status")
    Integer countByCampaignIdAndStatus(@Param("campaignId") Long campaignId, @Param("status") SchoolVaccinationStatus status);

    @Query("SELECT sv FROM SchoolVaccination sv WHERE sv.campaign.campaignId = :campaignId AND sv.status = :status AND sv.vaccinationDate = :date")
    List<SchoolVaccination> findByCampaignIdAndStatusAndDate(
            @Param("campaignId") Long campaignId,
            @Param("status") SchoolVaccinationStatus status,
            @Param("date") LocalDate date);

    List<SchoolVaccination> findByStatus(SchoolVaccinationStatus schoolVaccinationStatus);

    long countByStatus(SchoolVaccinationStatus status);
}
