package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.VaccinationCampaignStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "VaccinationCampaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccinationCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CampaignID")
    private Long campaignId;

    @Column(name = "CampaignName", nullable = false, length = 200, columnDefinition = "NVARCHAR(200)")
    private String campaignName;

    @Column(name = "VaccineName", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String vaccineName;

    @Column(name = "Description", columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Column(name = "VaccinationDate")
    private LocalDate vaccinationDate;

    @Column(name = "ConsentDeadline")
    private LocalDate consentDeadline;

    @Column(name = "TargetClassGroup")
    @Enumerated(EnumType.STRING)
    private ClassGroup targetClassGroup; // Thay đổi từ String targetClassGroup

    @Column(name = "Status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VaccinationCampaignStatus status = VaccinationCampaignStatus.DRAFT;

    @Column(name = "Notes", columnDefinition = "NVARCHAR(1000)")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrganizedByUserID", nullable = false)
    private User organizedByUser;

    @Column(name = "HealthcareProviderName", length = 200, columnDefinition = "NVARCHAR(200)")
    private String healthcareProviderName;

    @Column(name = "HealthcareProviderContact", length = 100)
    private String healthcareProviderContact;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VaccinationConsent> consentForms = new ArrayList<>();

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SchoolVaccination> vaccinations = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedByUserID")
    private User updatedByUser;

    @Column(name = "RescheduledAt")
    private LocalDateTime rescheduledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RescheduledByUserID")
    private User rescheduledByUser;
}
