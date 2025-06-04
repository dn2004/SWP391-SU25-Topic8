package com.fu.swp391.schoolhealthmanagementsystem.security;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull; // Thêm @NonNull để rõ ràng hơn
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException; // Cần cho việc bắt ParseException khi get claim
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String TOKEN_BLACKLIST_PREFIX = "jwt:blacklist:";

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService; // Service để load UserDetails

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            final String jwt = getJwtFromRequest(request);

            if (!StringUtils.hasText(jwt)) {
                filterChain.doFilter(request, response);
                return; // Không có token, cho qua filter tiếp theo
            }

            // 1. Kiểm tra blacklist trước
            String jti = null;
            String redisKey;
            try {
                JWTClaimsSet claimsSetForJti = jwtService.getClaimsToParseForBlacklist(jwt);
                if (claimsSetForJti != null) {
                    jti = claimsSetForJti.getJWTID();
                }
                redisKey = (jti != null && !jti.isEmpty()) ? TOKEN_BLACKLIST_PREFIX + jti : TOKEN_BLACKLIST_PREFIX + jwt;
            } catch (Exception e) {
                log.warn("Error parsing JTI for blacklist check (token: {}...), falling back to full token string. Error: {}",
                        truncateToken(jwt), e.getMessage());
                redisKey = TOKEN_BLACKLIST_PREFIX + jwt; // Fallback
            }

            Boolean isBlacklisted = redisTemplate.hasKey(redisKey);
            if (isBlacklisted != null && isBlacklisted) {
                log.warn("Access attempt with blacklisted token (key: {}). Token: {}...", redisKey, truncateToken(jwt));
                SecurityContextHolder.clearContext();
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Token has been invalidated (logged out).\"}");
                response.setContentType("application/json");
                return; // Token bị blacklist, dừng xử lý
            }

            // 2. Nếu không bị blacklist, tiến hành validate token đầy đủ
            if (jwtService.validateToken(jwt)) {
                JWTClaimsSet claims = jwtService.getClaimsFromToken(jwt); // Lấy claims sau khi đã validate
                String email = null;

                if (claims != null) {
                    try {
                        email = claims.getStringClaim("email");
                    } catch (ParseException e) {
                        log.error("Could not parse 'email' claim from validated JWT. Token: {}. Error: {}", truncateToken(jwt), e.getMessage(), e);
                        // Nếu không parse được email từ token đã validate thì có thể coi là lỗi nghiêm trọng
                    }
                } else {
                    log.warn("Token validated but could not retrieve claims. Token: {}...", truncateToken(jwt));
                    // Xảy ra nếu getClaimsFromToken trả về null (ví dụ: do hết hạn, issuer sai dù chữ ký đúng)
                    // Mặc dù validateToken đã kiểm tra, nhưng đây là một lớp bảo vệ nữa.
                }


                // 3. Nếu có email và chưa có ai được xác thực trong context, tiến hành xác thực
                if (StringUtils.hasText(email) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

                    // Kiểm tra lại tính hợp lệ của token với UserDetails (ví dụ: user có bị vô hiệu hóa không)
                    // Mặc dù jwtService.validateToken đã kiểm tra chữ ký, hết hạn, issuer.
                    // Việc kiểm tra userDetails.isEnabled() là tùy chọn ở đây, vì các filter khác có thể xử lý.

                    // Lấy roles từ claims (đảm bảo JwtService của bạn thêm claim "roles" đúng cách)
                    List<String> rolesFromToken;
                    try {
                        // Giả sử roles được lưu dưới dạng List<String>
                        Object rolesClaim = claims.getClaim("roles");
                        if (rolesClaim instanceof List) {
                            rolesFromToken = ((List<?>) rolesClaim).stream()
                                    .map(Object::toString)
                                    .collect(Collectors.toList());
                        } else {
                            log.warn("'roles' claim is not a List or is missing in token for user: {}", email);
                            rolesFromToken = List.of(); // Hoặc ném lỗi nếu roles là bắt buộc
                        }
                    } catch (Exception e) {
                        log.error("Error parsing 'roles' claim for user: {}. Token: {}", email, truncateToken(jwt), e);
                        rolesFromToken = List.of();
                    }

                    List<GrantedAuthority> authorities = rolesFromToken.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credentials (không cần cho JWT)
                            authorities // Sử dụng authorities từ token
                            // Hoặc bạn có thể dùng userDetails.getAuthorities() nếu UserDetails của bạn đã được load đúng quyền
                    );

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.debug("User '{}' authenticated successfully via JWT. Setting SecurityContext.", email);
                }

            } else {
                // validateToken trả về false (ví dụ: chữ ký sai, hết hạn, issuer sai)
                log.warn("JWT validation failed for token: {}...", truncateToken(jwt));
                // Không cần thiết lập SecurityContextHolder.clearContext() ở đây
                // vì nếu token không hợp lệ, SecurityContextHolder.getContext().getAuthentication() sẽ vẫn là null
                // và các quy tắc bảo vệ endpoint sẽ được áp dụng.
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext(); // Xóa context nếu có lỗi không mong muốn
            // Không nên trả về response lỗi trực tiếp từ filter này trừ khi thật cần thiết.
            // Hãy để GlobalExceptionHandler hoặc AuthenticationEntryPoint của Spring Security xử lý.
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String truncateToken(String token) {
        if (token == null) return "null";
        return token.length() > 15 ? token.substring(0, 15) + "..." : token;
    }
}