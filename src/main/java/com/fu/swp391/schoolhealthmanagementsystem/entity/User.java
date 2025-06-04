package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(min = 6, max = 255) // Password có thể null nếu user đăng ký/đăng nhập qua Firebase
    @Column(name = "Password")
    private String password;

    @NotBlank(message = "Họ và tên không được để trống") // Thêm message cho validation
    @Size(max = 100)
    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Email không được để trống") // Thêm message
    @Email(message = "Email không đúng định dạng")   // Thêm message
    @Size(max = 100)
    @Column(name = "Email", unique = true, nullable = false, length = 100)
    private String email;

    @Size(max = 20)
    @Column(name = "PhoneNumber", length = 20)
    private String phoneNumber;


    @NotNull(message = "Vai trò không được để trống") // Thêm message
    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false)
    private UserRole role;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsActive", nullable = false)
    @Builder.Default // Đặt giá trị mặc định khi sử dụng builder
    private boolean isActive = true;

    @Column(name = "firebase_uid", unique = true) // Nullable ban đầu
    private String firebaseUid;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Khởi tạo danh sách rỗng khi dùng builder
    private List<ParentStudentLink> childLinks = new ArrayList<>();

    @Column(name = "IsFullNameConfirmed", nullable = false)
    @Builder.Default
    private boolean isFullNameConfirmed = true;


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