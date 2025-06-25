package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.HealthIncident;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.HealthIncidentType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface HealthIncidentRepository extends JpaRepository<HealthIncident, Long>, JpaSpecificationExecutor<HealthIncident> {

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentId = :incidentId")
    Optional<HealthIncident> findIncidentEvenIfDeleted(@Param("incidentId") Long incidentId);


    Page<HealthIncident> findByStudent(Student student, Pageable pageable);

    // Trong HealthIncidentRepository.java
    @Query("SELECT DISTINCT hi FROM HealthIncident hi LEFT JOIN FETCH hi.supplyUsages su LEFT JOIN FETCH su.medicalSupply WHERE hi.incidentId = :incidentId")
    Optional<HealthIncident> findIncidentEvenIfDeletedWithUsages(@Param("incidentId") Long incidentId);
}