package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicalSupplyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "MedicalSupplies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalSupply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SupplyID")
    private Long supplyId;

    @Column(name = "Name", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(name = "Category", length = 50, columnDefinition = "NVARCHAR(50)")
    private String category;

    @Column(name = "Unit", length = 20, columnDefinition = "NVARCHAR(20)")
    private String unit; //Đơn vị tính của vật tư sau mỗi lần sử dụng. Ví dụ viên, ml,...

    @Column(name = "CurrentStock", nullable = false)
    @Builder.Default
    private Integer currentStock = 0;

    @Column(name = "Description", columnDefinition = "NVARCHAR(500)")
    private String description;

    @Column(name = "ExpiredDate")
    private LocalDate expiredDate;

    @Column(name = "Status", nullable = false, length = 20)
    @Builder.Default
    private MedicalSupplyStatus status = MedicalSupplyStatus.AVAILABLE;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedByUserID", updatable = false)
    private User createdByUser;

    @UpdateTimestamp
    @Column(name = "LastUpdatedAt")
    private LocalDateTime lastUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedByUserID")
    private User updatedByUser;

    @OneToMany(mappedBy = "medicalSupply", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<SupplyTransaction> supplyTransactions = new ArrayList<>();
}