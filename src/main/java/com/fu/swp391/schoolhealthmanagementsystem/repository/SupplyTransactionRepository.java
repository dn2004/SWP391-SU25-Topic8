package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.HealthIncident;
import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicalSupply;
import com.fu.swp391.schoolhealthmanagementsystem.entity.SupplyTransaction;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplyTransactionRepository extends JpaRepository<SupplyTransaction, Long> {
    List<SupplyTransaction> findByHealthIncident(HealthIncident incident);

    Page<SupplyTransaction> findByMedicalSupply(MedicalSupply medicalSupply, Pageable pageable);
}