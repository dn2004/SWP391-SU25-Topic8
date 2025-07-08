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

    List<SchoolVaccination> findByStatus(SchoolVaccinationStatus schoolVaccinationStatus);

    long countByStatus(SchoolVaccinationStatus status);
}
