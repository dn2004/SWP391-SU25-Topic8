package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipient_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient.userId = :userId AND n.read = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    long countByRecipient_UserIdAndIsReadFalse(Long userId);

    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}
