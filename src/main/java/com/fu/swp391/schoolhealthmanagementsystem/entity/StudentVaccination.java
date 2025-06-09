package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "student_vaccinations")
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

    @Column(name = "Provider")
    private String provider;

    @Column(name = "Note")
    private String notes;

    @Column(name = "ProofFileOriginalName", length = 255)
    private String proofFileOriginalName;

    @Column(name = "ProofFileUrl", length = 500)
    private String proofFileUrl;

    @Column(name = "ProofFileType", length = 100)
    private String proofFileType;

    @Column(name = "ProofPublicId", length = 255)
    private String proofPublicId;

    @Column(name = "ProofResourceType", length = 50)
    private String proofResourceType;


    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdateAt")
    private LocalDateTime updatedAt;

    @Column(name = "Status")
    @Builder.Default
    private StudentVaccinationStatus status = StudentVaccinationStatus.PENDING;




    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}