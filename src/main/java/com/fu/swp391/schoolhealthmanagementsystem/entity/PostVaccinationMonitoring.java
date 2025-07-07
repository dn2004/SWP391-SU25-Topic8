package com.fu.swp391.schoolhealthmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "PostVaccinationMonitoring")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostVaccinationMonitoring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MonitoringID")
    private Long monitoringId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SchoolVaccinationID", nullable = false, unique = true)
    private SchoolVaccination schoolVaccination;

    @Column(name = "MonitoringTime", nullable = false)
    private LocalDateTime monitoringTime;

    @Column(name = "Temperature")
    private Float temperature;

    @Column(name = "HasSideEffects", nullable = false)
    @Builder.Default
    private Boolean hasSideEffects = false;

    @Column(name = "SideEffectsDescription", columnDefinition = "NVARCHAR(1000)")
    private String sideEffectsDescription;

    @Column(name = "ActionsTaken", columnDefinition = "NVARCHAR(1000)")
    private String actionsTaken;

    @Column(name = "Notes", columnDefinition = "NVARCHAR(1000)")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RecordedByUserID", nullable = false)
    private User recordedByUser;

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
