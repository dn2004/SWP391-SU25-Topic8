package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ConsentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "VaccinationConsents",
    uniqueConstraints = @UniqueConstraint(columnNames = {"CampaignID", "StudentID"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccinationConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConsentID")
    private Long consentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CampaignID", nullable = false)
    private VaccinationCampaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentID", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentUserID")
    private User parent;

    @Column(name = "Status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConsentStatus status = ConsentStatus.PENDING;

    @Column(name = "ParentResponse", columnDefinition = "NVARCHAR(1000)")
    private String parentResponse;

    @Column(name = "MedicalNotes", columnDefinition = "NVARCHAR(1000)")
    private String medicalNotes;

    @Column(name = "ConsentFormSentAt")
    private LocalDateTime consentFormSentAt;

    @Column(name = "ResponseReceivedAt")
    private LocalDateTime responseReceivedAt;

    @Column(name = "ReminderSentAt")
    private LocalDateTime reminderSentAt;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
