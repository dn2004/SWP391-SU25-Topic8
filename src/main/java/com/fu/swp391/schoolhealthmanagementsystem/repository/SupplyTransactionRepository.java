package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.HealthIncident;
import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicalSupply;
import com.fu.swp391.schoolhealthmanagementsystem.entity.SupplyTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SupplyTransactionRepository extends JpaRepository<SupplyTransaction, Long>, JpaSpecificationExecutor<SupplyTransaction> {
    boolean existsByMedicalSupplyAndHealthIncidentNotNull(MedicalSupply medicalSupply);

    long countBySupplyTransactionType(com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType type);
}