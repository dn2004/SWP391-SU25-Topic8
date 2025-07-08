package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicationTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationTimeSlotRepository extends JpaRepository<MedicationTimeSlot, Long> {
    long countBySchoolSessionHint(com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolSession session);
}