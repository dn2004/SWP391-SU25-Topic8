package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import jakarta.persistence.*;

import lombok.*; // Import chung cho các annotation của Lombok
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "Users",
        uniqueConstraints = @UniqueConstraint(columnNames = "Email")
)
@Getter
@Setter
@NoArgsConstructor // Quan trọng cho JPA và @Builder
@AllArgsConstructor // Hữu ích cho @Builder và nếu bạn muốn có constructor đầy đủ
@Builder // Thêm Builder pattern
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Long userId;

    @Column(name = "Password")
    private String password;

    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;

    @Column(name = "Email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "PhoneNumber", unique = true ,length = 20)
    private String phoneNumber;

    @Column(name = "Role", nullable = false)
    private UserRole role;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsActive", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ParentStudentLink> childLinks = new ArrayList<>();


    // Helper method để kiểm tra phụ huynh đã liên kết chưa
    @Transient // Không được lưu vào DB, tính toán lúc runtime
    public boolean isLinkedToStudent() {
        if (this.role == UserRole.Parent) {
            // Đảm bảo childLinks không null trước khi kiểm tra isEmpty()
            // Mặc dù @Builder.Default đã khởi tạo, đây là một lớp bảo vệ thêm.
            return this.childLinks != null && !this.childLinks.isEmpty();
        }
        return true; // Không áp dụng cho các vai trò không phải Phụ huynh (mặc định là "đã link" để không bị chặn)
    }

    // --- UserDetails methods ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) { // Phòng trường hợp role chưa được set (dù @NotNull nên ngăn chặn điều này)
            return Collections.emptyList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // Sử dụng email làm username cho việc xác thực
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Tài khoản không bao giờ hết hạn theo logic hiện tại
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive; // Tài khoản bị khóa nếu không active
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Thông tin đăng nhập không bao giờ hết hạn theo logic hiện tại
    }

    @Override
    public boolean isEnabled() {
        return isActive; // Tài khoản được kích hoạt nếu active
    }
}