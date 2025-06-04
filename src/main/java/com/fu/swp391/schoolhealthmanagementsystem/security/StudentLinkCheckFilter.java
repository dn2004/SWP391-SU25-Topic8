package com.fu.swp391.schoolhealthmanagementsystem.security;

import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class StudentLinkCheckFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_EXACT_PATHS_FOR_UNLINKED_PARENTS = new HashSet<>(Arrays.asList(
            "/api/auth/logout",
            "/api/parent/link-student"
    ));

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestPath = request.getRequestURI();


        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response); // Not authenticated, let other filters handle
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User authenticatedUser)) {
            // If principal is not our User object, e.g. just a username string from basic auth or other custom setup.
            // This filter assumes UserDetails is our User entity.
            // If UserDetails is a different object, you'd need to load User from DB by username.
            filterChain.doFilter(request, response);
            return;
        }

        if (authenticatedUser.getRole() == UserRole.Parent) {
            // authenticatedUser.childLinks đã được khởi tạo bởi UserDetailsServiceImpl
            if (!authenticatedUser.isLinkedToStudent()) {
                if (isPathAllowedForUnlinkedParent(requestPath, request.getMethod())) { // Truyền thêm method
                    log.trace("Phụ huynh chưa liên kết {} được phép truy cập vào: {} {}", authenticatedUser.getEmail(), request.getMethod(), requestPath);
                    filterChain.doFilter(request, response);
                } else {
                    // Nếu đường dẫn không được phép, trả về lỗi FORBIDDEN
                    log.warn("Phụ huynh chưa liên kết {} cố gắng truy cập tài nguyên bị hạn chế: {} {}",
                            authenticatedUser.getEmail(), request.getMethod(), requestPath);

                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json; charset=UTF-8");

                    String errorJson = String.format(
                            "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                            java.time.LocalDateTime.now().toString(),
                            HttpStatus.FORBIDDEN.value(),
                            HttpStatus.FORBIDDEN.getReasonPhrase(),
                            "Bạn cần liên kết với một học sinh trước khi sử dụng tính năng này.",
                            requestPath
                    );
                    response.getWriter().write(errorJson);
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isPathAllowedForUnlinkedParent(String requestPath, String method) {
        return ALLOWED_EXACT_PATHS_FOR_UNLINKED_PARENTS.contains(requestPath);
    }

}