package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.HealthIncidentType;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "HealthIncidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("Deleted = false")
public class HealthIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IncidentID")
    private Long incidentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentID", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RecordedByUserID", nullable = false, updatable = false)
    private User recordedByUser;

    @Column(name = "IncidentDateTime", nullable = false)
    private LocalDateTime incidentDateTime;

    @Column(name = "IncidentType", nullable = false, length = 50)
    private HealthIncidentType incidentType;

    @Column(name = "Description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "ActionTaken", columnDefinition = "TEXT", nullable = false)
    private String actionTaken;

    @Column(name = "Location", length = 100, columnDefinition = "NVARCHAR(100)")
    private String location;

    @OneToMany(mappedBy = "healthIncident", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)

    @Builder.Default
    private List<SupplyTransaction> supplyUsages = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedByUserID")
    private User updatedByUser;

    @Column(name = "Deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DeletedByUserID")
    private User deletedByUser;
}