package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolVaccinationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SchoolVaccinations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolVaccination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SchoolVaccinationId")
    private Long schoolVaccinationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CampaignID", nullable = false)
    private VaccinationCampaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentId", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ConsentID", nullable = false)
    private VaccinationConsent consent;

    @Column(name = "Status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SchoolVaccinationStatus status = SchoolVaccinationStatus.SCHEDULED;

    @Column(name = "VaccinationDate")
    private LocalDate vaccinationDate;

    @Column(name = "DoseNumber")
    @Builder.Default
    private Integer doseNumber = 1;

    @Column(name = "Notes", columnDefinition = "NVARCHAR(500)")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AdministeredByUserID")
    private User administeredByUser;

    @OneToOne(mappedBy = "schoolVaccination", cascade = CascadeType.ALL, orphanRemoval = true)
    private PostVaccinationMonitoring monitoringRecord;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedByUserID")
    private User updatedByUser;
}
