package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByFirebaseUid(String firebaseUid);
    Page<User> findAllByRole(UserRole role, Pageable pageable);

    // For checking email uniqueness during Firebase login/linking
    boolean existsByEmailAndFirebaseUidNot(String email, String firebaseUid);
    boolean existsByEmailAndFirebaseUidIsNull(String email);
}