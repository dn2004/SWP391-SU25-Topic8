package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentChronicDiseaseStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "StudentChronicDiseases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentChronicDisease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ChronicDiseaseId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentId", nullable = false)
    private Student student;

    @Column(name = "DiseaseName", nullable = false, length = 150)
    private String diseaseName;

    @Column(name = "DiagnosedDate")
    private LocalDate diagnosedDate;

    @Column(name = "DiagnosingDoctor", length = 100)
    private String diagnosingDoctor;

    @Column(name = "Notes", length = 1000)
    private String notes;

    @Column(name = "AttachmentFileOriginalName", length = 255)
    private String attachmentFileOriginalName;

    @Column(name = "AttachmentFileType", length = 100)
    private String attachmentFileType;

    @Column(name = "AttachmentPublicId", length = 255)
    private String attachmentPublicId;

    @Column(name = "AttachmentResourceType", length = 50)
    private String attachmentResourceType;

    @Column(name = "Status", nullable = false, length = 50)
    @Builder.Default
    private StudentChronicDiseaseStatus status = StudentChronicDiseaseStatus.PENDING;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedByUserId", nullable = false, updatable = false)
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedByUserId")
    private User updatedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ApprovedByUserId")
    private User approvedByUser;

    @Column(name = "ApprovedAt")
    private LocalDateTime approvedAt;

    @Column(name = "ApproverNotes", length = 500)
    private String approverNotes;
}
