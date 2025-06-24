package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp; // Đảm bảo import đúng

import java.time.LocalDateTime;

@Entity
@Table(name = "SupplyTransactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TransactionID")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SupplyID", nullable = false)
    private MedicalSupply medicalSupply;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "SupplyTransactionType", nullable = false, length = 50)
    private SupplyTransactionType supplyTransactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IncidentID")
    private HealthIncident healthIncident;

    @Column(name = "Note", columnDefinition = "NVARCHAR(500)")
    private String note;

    @CreationTimestamp
    @Column(name = "TransactionDateTime", nullable = false, updatable = false)
    private LocalDateTime transactionDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PerformedByUserID", nullable = false)
    private User performedByUser;
}