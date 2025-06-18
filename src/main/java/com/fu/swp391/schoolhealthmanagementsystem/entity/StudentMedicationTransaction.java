package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentMedicationTransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "StudentMedicationTransactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentMedicationTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TransactionID")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentMedicationID", nullable = false)
    private StudentMedication studentMedication;

    @Enumerated(EnumType.STRING)
    @Column(name = "TransactionType", nullable = false)
    private StudentMedicationTransactionType transactionType;

    @Column(name = "DosesChanged", nullable = false)
    @Builder.Default
    private Integer dosesChanged = 1; // Số liều bị thay đổi (+ hoặc - tùy logic, hoặc luôn dương và type quyết định)


    // DosesBefore và DosesAfter đã được bỏ

    @CreationTimestamp
    @Column(name = "TransactionDateTime", nullable = false, updatable = false)
    private LocalDateTime transactionDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PerformedByUserID", nullable = false)
    private User performedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ScheduledMedicationTaskID")
    private ScheduledMedicationTask scheduledMedicationTask;

    @Column(name = "Notes", length = 500, columnDefinition = "NVARCHAR(500)")
    private String notes;
}