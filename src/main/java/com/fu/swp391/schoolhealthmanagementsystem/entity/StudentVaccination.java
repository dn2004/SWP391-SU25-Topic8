package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp; // Đảm bảo import đúng

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "StudentVaccinations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentVaccination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StudentVaccinationId")
    private Long studentVaccinationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentId", nullable = false)
    private Student student;

    @Column(name = "VaccineName", nullable = false, length = 100)
    private String vaccineName;

    @Column(name = "VaccinationDate")
    private LocalDate vaccinationDate;

    @Column(name = "Provider", length = 200)
    private String provider;

    @Column(name = "Note", length = 500)
    private String notes;

    @Column(name = "ProofFileOriginalName", length = 255)
    private String proofFileOriginalName;

    @Column(name = "ProofFileType", length = 100)
    private String proofFileType;

    @Column(name = "ProofPublicId", length = 255)
    private String proofPublicId;

    @Column(name = "ProofResourceType", length = 50)
    private String proofResourceType;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "Status", nullable = false, length = 50)
    @Builder.Default
    private StudentVaccinationStatus status = StudentVaccinationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ApprovedByUserId")
    private User approvedByUser;

    @Column(name = "ApprovedAt")
    private LocalDateTime approvedAt;

    @Column(name = "ApproverNotes", length = 500)
    private String approverNotes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // updatedAt = LocalDateTime.now(); // Không cần thiết ở đây, @UpdateTimestamp sẽ xử lý
    }

    @PreUpdate
    protected void onUpdate() {
        // updatedAt = LocalDateTime.now(); // @UpdateTimestamp sẽ tự động xử lý việc này
    }
}