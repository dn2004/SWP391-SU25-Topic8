package com.fu.swp391.schoolhealthmanagementsystem.security;

import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class StudentLinkCheckFilter extends OncePerRequestFilter {

    // Danh sách các đường dẫn public, sao chép từ SecurityConfig
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "/api/auth/**",
            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
            "/api/blogs",
            "/api/enums/**",
            "/api/blogs/",
            "/api/blogs/{id}",
            "/api/blogs/slug/{slug}"
    ));

    // Danh sách các đường dẫn được phép cho phụ huynh chưa liên kết (ngoài các đường dẫn public)
    private static final Set<String> ALLOWED_SPECIAL_PATHS_FOR_UNLINKED_PARENTS = new HashSet<>(Arrays.asList(
            "/api/auth/logout",
            "/api/parent/link-student",
            "/api/user/profile/change-password",
            "/api/user/profile/me"
    ));

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Bỏ qua filter nếu request là OPTIONS (pre-flight) hoặc là một đường dẫn public
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User authenticatedUser)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Logic chính: chỉ áp dụng cho vai trò Phụ huynh chưa liên kết
        if (authenticatedUser.getRole() == UserRole.Parent && !authenticatedUser.isLinkedToStudent()) {
            if (isPathAllowedForUnlinkedParent(requestPath)) {
                log.trace("Phụ huynh chưa liên kết {} được phép truy cập vào: {} {}", authenticatedUser.getEmail(), request.getMethod(), requestPath);
                filterChain.doFilter(request, response);
            } else {
                log.warn("Phụ huynh chưa liên kết {} cố gắng truy cập tài nguyên bị hạn chế: {} {}",
                        authenticatedUser.getEmail(), request.getMethod(), requestPath);
                sendForbiddenResponse(response, requestPath);
            }
        } else {
            // Đối với các vai trò khác hoặc phụ huynh đã liên kết, cho phép đi tiếp
            filterChain.doFilter(request, response);
        }
    }

    private boolean isPublicPath(String requestPath) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

    private boolean isPathAllowedForUnlinkedParent(String requestPath) {
        // Phụ huynh chưa liên kết được phép truy cập các đường dẫn đặc biệt
        return ALLOWED_SPECIAL_PATHS_FOR_UNLINKED_PARENTS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

    private void sendForbiddenResponse(HttpServletResponse response, String requestPath) throws IOException {
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
    }
}