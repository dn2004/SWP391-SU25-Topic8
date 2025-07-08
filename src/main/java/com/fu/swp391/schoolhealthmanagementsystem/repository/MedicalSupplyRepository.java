package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicalSupply;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicalSupplyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicalSupplyRepository extends JpaRepository<MedicalSupply, Long>, JpaSpecificationExecutor<MedicalSupply> {
    Optional<MedicalSupply> findFirstByName(String supplyName);

    List<MedicalSupply> findAllByExpiredDateLessThanEqualAndStatusNot(LocalDate date, MedicalSupplyStatus status);

    long countByStatus(MedicalSupplyStatus status);
    long countByCurrentStockLessThanEqual(int threshold);
}