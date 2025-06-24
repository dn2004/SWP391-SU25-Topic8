package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "StudentMedications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentMedication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StudentMedicationID")
    private Long studentMedicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentID", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SubmittedByParentID", nullable = false)
    private User submittedByParent;

    @Column(name = "MedicationName", nullable = false, length = 200, columnDefinition = "NVARCHAR(200)")
    private String medicationName;

    @Column(name = "DosagePerAdministrationText", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String dosagePerAdministrationText;

    @Column(name = "TotalDosesProvided")
    private Integer totalDosesProvided;

    @Column(name = "RemainingDoses")
    private Integer remainingDoses;

    @Column(name = "ExpiryDate")
    private LocalDate expiryDate;

    @Column(name = "DateReceived", nullable = false)
    private LocalDate dateReceived;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReceivedByMedicalStaffID")
    private User receivedByMedicalStaff;


    @Column(name = "Status", nullable = false)
    private MedicationStatus status; // Trạng thái thuốc, ví dụ: AVAILABLE, OUT_OF_DOSES

    @Column(name = "Notes", length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String notes;

    @Column(name = "UsageInstruction", length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String usageInstruction;

    @Column(name = "ScheduleStartDate", nullable = false) // Bắt buộc nếu status = AVAILABLE
    private LocalDate scheduleStartDate;

    @OneToMany(mappedBy = "studentMedication", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MedicationTimeSlot> medicationTimeSlots = new ArrayList<>();


    @Column(name = "NextScheduledTaskGenerationDate")
    private LocalDate nextScheduledTaskGenerationDate;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedByUserID", nullable = false, updatable = false)
    private User createdByUser;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedByUserID")
    private User updatedByUser;

    @OneToMany(mappedBy = "studentMedication", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ScheduledMedicationTask> scheduledTasks = new ArrayList<>();

    @OneToMany(mappedBy = "studentMedication", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StudentMedicationTransaction> medicationTransactions = new ArrayList<>();

    // Helper methods (giữ nguyên)
    public void addMedicationTimeSlot(MedicationTimeSlot slot) {
        if (slot != null) {
            this.medicationTimeSlots.add(slot);
            slot.setStudentMedication(this); // Quan trọng: Thiết lập tham chiếu ngược từ slot về StudentMedication
        }
    }
    public void removeMedicationTimeSlot(MedicationTimeSlot slot) {
        if (slot != null && this.medicationTimeSlots.contains(slot)) {
            this.medicationTimeSlots.remove(slot);
            slot.setStudentMedication(null); // Quan trọng: Ngắt tham chiếu ngược
        }
    }
    public void clearMedicationTimeSlots() {
        List<MedicationTimeSlot> slotsToRemove = new ArrayList<>(this.medicationTimeSlots);
        for (MedicationTimeSlot slot : slotsToRemove) {
            this.removeMedicationTimeSlot(slot);
        }
    }
}