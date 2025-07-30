package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicationTimeSlot;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationTimeSlotRepository extends JpaRepository<MedicationTimeSlot, Long> {
    long countBySchoolSessionHint(SchoolSession session);
}