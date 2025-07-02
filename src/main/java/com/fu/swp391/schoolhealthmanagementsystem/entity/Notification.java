package com.fu.swp391.schoolhealthmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "Notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RecipientId", nullable = false)
    @ToString.Exclude
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SenderId") // Có thể null nếu là hệ thống
    @ToString.Exclude
    private User sender;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "`Read`", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(length = 255)
    private String link; // URL để điều hướng khi nhấp vào

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
