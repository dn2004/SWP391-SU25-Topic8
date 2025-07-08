package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedication;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedicationTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudentMedicationTransactionRepository extends JpaRepository<StudentMedicationTransaction, Long>,
        JpaSpecificationExecutor<StudentMedicationTransaction> {

    Page<StudentMedicationTransaction> findByStudentMedication(StudentMedication studentMedication, Pageable pageable);

    long countByTransactionType(com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentMedicationTransactionType transactionType);
}
