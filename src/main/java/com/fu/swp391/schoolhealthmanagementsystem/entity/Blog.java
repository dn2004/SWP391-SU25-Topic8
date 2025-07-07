package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import com.github.slugify.Slugify;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Blogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Thumnail", nullable = false)
    private String thumbnail; //thumbnail URL or path

    @Column(name = "Description", nullable = false, columnDefinition = "VARCHAR(500)")
    private String description;

    @Column(name = "Slug", nullable = false, unique = true)
    private String slug;

    @Lob
    @Column(name = "Content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    private User author;

    @Column(name = "Status", nullable = false)
    private BlogStatus status;

    @Column(name = "Category", nullable = false)
    private BlogCategory category;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    public void generateSlugOnPersist() {
        this.generateSlug();
    }

    @PreUpdate
    public void generateSlugOnUpdate() {
        // Chỉ tạo lại slug nếu tiêu đề thay đổi (tùy chọn nhưng nên có)
        // Việc kiểm tra này cần logic phức tạp hơn, ở đây ta làm đơn giản là luôn tạo lại
        this.generateSlug();
    }

    private void generateSlug() {
        if (this.title != null && !this.title.isEmpty()) {
            final Slugify slugify = Slugify.builder().build();
            this.slug = slugify.slugify(this.title);
        }
    }
}
