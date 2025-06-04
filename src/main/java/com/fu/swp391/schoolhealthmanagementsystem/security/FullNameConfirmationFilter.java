package com.fu.swp391.schoolhealthmanagementsystem.security;

import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class FullNameConfirmationFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_PATHS_WHEN_FULLNAME_NOT_CONFIRMED = new HashSet<>(Arrays.asList(
            "/api/user/profile/confirm-fullname", // API để xác nhận tên
//            "/api/user/profile/me",              // Lấy thông tin user (để biết trạng thái)
            "/api/auth/logout"                  // Logout

    ));

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestPath = request.getRequestURI();

        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User authenticatedUser)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Filter này áp dụng cho tất cả các vai trò nếu họ chưa xác nhận tên đầy đủ
        // (Mặc dù kịch bản này chủ yếu cho Parent đăng nhập Firebase lần đầu)
        if (!authenticatedUser.isFullNameConfirmed()) {
            if (isPathAllowed(requestPath, request.getMethod())) {
                filterChain.doFilter(request, response);
            } else {
                log.warn("Người dùng {} chưa xác nhận tên đầy đủ, cố gắng truy cập tài nguyên bị hạn chế: {} {}",
                        authenticatedUser.getEmail(), request.getMethod(), requestPath);
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json; charset=UTF-8");
                String errorJson = String.format(
                        "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                        java.time.LocalDateTime.now().toString(),
                        HttpStatus.FORBIDDEN.value(),
                        HttpStatus.FORBIDDEN.getReasonPhrase(),
                        "Bạn cần cung cấp và xác nhận tên đầy đủ trước khi tiếp tục.",
                        requestPath
                );
                response.getWriter().write(errorJson);
                return;
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isPathAllowed(String requestPath, String method) {
        return ALLOWED_PATHS_WHEN_FULLNAME_NOT_CONFIRMED.contains(requestPath);
    }
}