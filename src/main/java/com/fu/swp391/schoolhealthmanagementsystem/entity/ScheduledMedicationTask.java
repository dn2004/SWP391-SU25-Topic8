package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolSession;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ScheduledMedicationTasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledMedicationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ScheduledTaskID")
    private Long scheduledTaskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentMedicationID", nullable = false)
    private StudentMedication studentMedication;

    @Column(name = "ScheduledDate", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "ScheduledTimeText", length = 50, columnDefinition = "NVARCHAR(50)")
    private String scheduledTimeText; // Vẫn giữ để NVYT dễ đọc

    @Column(name = "SchoolSession", length = 20) // Có thể suy ra từ ScheduledTimeText hoặc từ MedicationTimesPerDay
    private SchoolSession schoolSession; // ENUM: MORNING, AFTERNOON

    @Column(name = "Status", nullable = false)
    private ScheduledMedicationTaskStatus status;

    // Thông tin khi đã thực hiện
    @Column(name = "AdministeredAt")
    private LocalDateTime administeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AdministeredByStaffID")
    private User administeredByStaff;

    // Các trường cho Proof File giống StudentVaccination
    @Column(name = "ProofFileOriginalName", length = 255)
    private String proofFileOriginalName;

    @Column(name = "ProofFileType", length = 100)
    private String proofFileType;

    @Column(name = "ProofPublicId", length = 255)
    private String proofPublicId;

    @Column(name = "ProofResourceType", length = 50)
    private String proofResourceType;

    @Column(name = "StaffNotes", length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String staffNotes;

    @CreationTimestamp
    @Column(name = "RequestedAt", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}