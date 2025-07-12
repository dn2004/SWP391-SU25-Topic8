package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.HealthIncident;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthIncidentRepository extends JpaRepository<HealthIncident, Long>, JpaSpecificationExecutor<HealthIncident> {

    Page<HealthIncident> findByStudent(Student student, Pageable pageable);

    // Trong HealthIncidentRepository.java
    @EntityGraph(attributePaths = {"supplyUsages", "supplyUsages.medicalSupply"})
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentId = :incidentId")
    Optional<HealthIncident> findIncidentEvenIfDeletedWithUsages(@Param("incidentId") Long incidentId);

    long countByIncidentType(com.fu.swp391.schoolhealthmanagementsystem.entity.enums.HealthIncidentType type);

    long countByCreatedAtAfter(java.time.LocalDateTime dateTime);

    long countByStudent(Student student);
}