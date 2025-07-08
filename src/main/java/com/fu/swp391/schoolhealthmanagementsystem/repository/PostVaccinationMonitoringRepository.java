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

    long countByHasSideEffectsTrue();
}
