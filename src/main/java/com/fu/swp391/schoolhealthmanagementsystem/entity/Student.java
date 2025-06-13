package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Gender;
import jakarta.persistence.*;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Students",
        uniqueConstraints = @UniqueConstraint(columnNames = "StudentCode")
)
@Getter
@Setter
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StudentID")
    private Long studentId;

    @Column(name = "StudentCode", unique = true, nullable = false, length = 50)
    private String studentCode;

    @Column(name = "FullName", nullable = false, length = 50, columnDefinition = "NVARCHAR(100)")
    private String fullName;

    @Column(name = "DateOfBirth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "Gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "ClassName", nullable = false, length = 50)
    private String className;

    @Column(name = "Address", length = 255)
    private String address;

    @Column(name = "Active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ParentStudentLink> parentLinks = new ArrayList<>();

    @Column(name = "InvitationCode", unique = true, length = 20)
    private String invitationCode;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<StudentVaccination> vaccinations = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HealthIncident> healthIncidents = new ArrayList<>();
}