package com.fu.swp391.schoolhealthmanagementsystem.security;

import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Important for LAZY loading

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Đang tải thông tin người dùng cho email: {}", email);
        User user = userRepository.findByEmail(email) // Giả sử findByEmail đã có @EntityGraph hoặc bạn dùng cách này
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy người dùng với email: {}", email);
                    return new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email);
                });

        // Khởi tạo childLinks một cách tường minh bên trong transaction
        if (user.getRole() == UserRole.Parent) {
            // Cách 1: "Chạm" vào collection
//             user.getChildLinks().size(); // Hoặc user.getChildLinks().isEmpty();
            // Cách 2: Dùng Hibernate.initialize (an toàn hơn và rõ ràng hơn)
            Hibernate.initialize(user.getChildLinks());
        }

        log.info("Đã tải thành công UserDetails cho: {} với childLinks đã được khởi tạo (nếu là Parent).", email);
        return user;
    }
}