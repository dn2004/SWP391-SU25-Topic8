package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Gender;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student", uniqueConstraints = {
        @UniqueConstraint(columnNames = "InvitationCode")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StudentID")
    private Long id;

    @Column(name = "FullName", nullable = false, length = 50, columnDefinition = "NVARCHAR(100)")
    private String fullName;

    @Column(name = "DateOfBirth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "Gender", nullable = false)
    private Gender gender;

    @Column(name = "ClassGroup", nullable = false)
    private ClassGroup classGroup;

    @Column(name = "ClassValue", nullable = false)
    private Class classValue;

    @Column(name = "Status", nullable = false, length = 20)
    @Builder.Default
    private StudentStatus status = StudentStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ParentStudentLink> parentLinks = new ArrayList<>();

    @Column(name = "InvitationCode", unique = true, length = 20)
    private String invitationCode;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    List<StudentVaccination> vaccinations = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<HealthIncident> healthIncidents = new ArrayList<>();

    /**
     * Trả về tên lớp đầy đủ bằng cách kết hợp khối và lớp
     * Ví dụ: "Mầm A", "Chồi B", "Lá C"
     */
    @Transient
    public String getClassName() {
        if (classGroup == null || classValue == null) {
            return null;
        }
        return classGroup.getDisplayName() + " " + classValue;
    }
}