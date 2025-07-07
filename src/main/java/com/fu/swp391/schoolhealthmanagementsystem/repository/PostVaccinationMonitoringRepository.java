package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.PostVaccinationMonitoring;
import com.fu.swp391.schoolhealthmanagementsystem.entity.SchoolVaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostVaccinationMonitoringRepository extends JpaRepository<PostVaccinationMonitoring, Long> {

    Optional<PostVaccinationMonitoring> findBySchoolVaccination(SchoolVaccination schoolVaccination);

    @Query("SELECT pvm FROM PostVaccinationMonitoring pvm WHERE pvm.schoolVaccination.schoolVaccinationId = :vaccinationId")
    Optional<PostVaccinationMonitoring> findByVaccinationId(@Param("vaccinationId") Long vaccinationId);

    @Query("SELECT pvm FROM PostVaccinationMonitoring pvm " +
           "WHERE pvm.schoolVaccination.student.id = :studentId " +
           "ORDER BY pvm.monitoringTime DESC")
    List<PostVaccinationMonitoring> findByStudentIdOrderByTimeDesc(@Param("studentId") Long studentId);

    @Query("SELECT pvm FROM PostVaccinationMonitoring pvm " +
           "WHERE pvm.schoolVaccination.campaign.campaignId = :campaignId " +
           "AND pvm.monitoringTime BETWEEN :startTime AND :endTime " +
           "AND pvm.hasSideEffects = true")
    List<PostVaccinationMonitoring> findSideEffectsByCampaignIdAndTimeRange(
            @Param("campaignId") Long campaignId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
