package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicalSupply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalSupplyRepository extends JpaRepository<MedicalSupply, Long> {
    Optional<MedicalSupply> findBySupplyId(Long supplyId);

    Optional<MedicalSupply> findByNameAndUnit(String name, String unit);

    Page<MedicalSupply> findAllByActive(Boolean isActiveFilter, Pageable pageable);

    Optional<MedicalSupply> findFirstByName(String supplyName);
}