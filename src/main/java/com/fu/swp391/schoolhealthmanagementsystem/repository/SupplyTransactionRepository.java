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
    List<SupplyTransaction> findByHealthIncident(HealthIncident incident);

    Page<SupplyTransaction> findByMedicalSupply(MedicalSupply medicalSupply, Pageable pageable);

    /**
     * Kiểm tra xem một vật tư y tế có giao dịch nào liên quan đến sự cố y tế không
     *
     * @param medicalSupply Vật tư y tế cần kiểm tra
     * @return true nếu có giao dịch liên quan đến sự cố y tế, false nếu không
     */
    boolean existsByMedicalSupplyAndHealthIncidentNotNull(MedicalSupply medicalSupply);

    long countBySupplyTransactionType(com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType type);
}