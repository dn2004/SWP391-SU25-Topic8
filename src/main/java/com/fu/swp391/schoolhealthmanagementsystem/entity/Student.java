package com.fu.swp391.schoolhealthmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Student code is required")
    @Size(max = 50)
    @Column(name = "StudentCode", unique = true, nullable = false, length = 50)
    private String studentCode;

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;

    @NotNull(message = "Date of birth is required")
    @PastOrPresent(message = "Date of birth must be in the past or present")
    @Column(name = "DateOfBirth", nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    @Size(max = 10)
    @Column(name = "Gender", nullable = false, length = 10)
    private String gender;

    @NotBlank(message = "Class name is required")
    @Size(max = 50)
    @Column(name = "ClassName", nullable = false, length = 50)
    private String className;

    @Size(max = 255)
    @Column(name = "Address", length = 255)
    private String address;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive = true;

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
}