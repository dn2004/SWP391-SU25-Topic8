package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long>, JpaSpecificationExecutor<Blog> {
    Page<Blog> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Optional<Blog> findBySlug(String slug);

    long countByStatus(com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus status);

    long countByCategory(com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory category);

    long countByCreatedAtAfter(java.time.LocalDateTime dateTime);
}
