package com.fu.swp391.schoolhealthmanagementsystem.security;

import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID; // For JTI
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtService {


    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    public String generateToken(User user) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            JWSSigner signer = new MACSigner(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

            List<String> authorities = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUserId().toString())
                    .issuer(jwtIssuer)
                    .issueTime(now)
                    .expirationTime(expiryDate)
                    .jwtID(UUID.randomUUID().toString()) // <<<<< THÊM JTI VÀO ĐÂY
                    .claim("email", user.getEmail())
                    .claim("roles", authorities)
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Error generating JWT: {}", e.getMessage());
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    public String getEmailFromToken(String token) {
        JWTClaimsSet claims = getClaimsFromToken(token); // Uses the validating getClaimsFromToken
        if (claims != null) {
            try {
                return claims.getStringClaim("email");
            } catch (ParseException e) {
                log.error("Could not parse email claim from JWT: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    public Long getUserIdFromToken(String token) {
        JWTClaimsSet claims = getClaimsFromToken(token); // Uses the validating getClaimsFromToken
        if (claims != null) {
            try {
                String subject = claims.getSubject();
                return subject != null ? Long.parseLong(subject) : null;
            } catch (NumberFormatException e) {
                log.error("Could not parse user ID (subject) claim from JWT to Long: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes(StandardCharsets.UTF_8));
            if (!signedJWT.verify(verifier)) {
                log.warn("JWT signature validation failed for token: {}...", token.length() > 7 ? token.substring(0,7) : token);
                return false;
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet(); // Get claims AFTER verification
            if (claimsSet.getExpirationTime().before(new Date())) {
                log.warn("JWT token has expired. Token: {}...", token.length() > 7 ? token.substring(0,7) : token);
                return false;
            }
            if (!jwtIssuer.equals(claimsSet.getIssuer())) {
                log.warn("JWT issuer mismatch. Expected: {}, Actual: {}. Token: {}...", jwtIssuer, claimsSet.getIssuer(), token.length() > 7 ? token.substring(0,7) : token);
                return false;
            }
            log.trace("Token validated successfully: {}...", token.length() > 7 ? token.substring(0,7) : token);
            return true;
        } catch (ParseException e) {
            log.error("Error parsing JWT during validation: {}. Token: {}...", e.getMessage(), token.length() > 7 ? token.substring(0,7) : token);
        } catch (JOSEException e) {
            log.error("Error verifying JWT signature during validation: {}. Token: {}...", e.getMessage(), token.length() > 7 ? token.substring(0,7) : token);
        }
        return false;
    }

    /**
     * Gets claims from a token only if the token is valid (signature, not expired, correct issuer).
     * This is suitable for general claim extraction AFTER validation.
     */
    // Trong JwtService.java -> getClaimsFromToken
    public JWTClaimsSet getClaimsFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            // SỬA Ở ĐÂY: Đảm bảo dùng UTF-8
            JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes(StandardCharsets.UTF_8));
            if (signedJWT.verify(verifier)) {
                JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
                if (claimsSet.getExpirationTime().before(new Date())) {
                    log.warn("Attempted to get claims from an expired token (verified signature but expired). Token: {}...", token.length() > 7 ? token.substring(0,7) : token);
                    return null;
                }
                if (!jwtIssuer.equals(claimsSet.getIssuer())) {
                    log.warn("Attempted to get claims from a token with issuer mismatch. Token: {}...", token.length() > 7 ? token.substring(0,7) : token);
                    return null;
                }
                return claimsSet;
            } else {
                log.warn("JWT signature verification failed when trying to get claims. Token: {}...", token.length() > 7 ? token.substring(0,7) : token);
            }
        } catch (ParseException e) {
            log.error("Error parsing JWT when trying to get claims: {}. Token: {}...", e.getMessage(), token.length() > 7 ? token.substring(0,7) : token);
        } catch (JOSEException e) {
            log.error("Error verifying JWT signature when trying to get claims: {}. Token: {}...", e.getMessage(), token.length() > 7 ? token.substring(0,7) : token);
        }
        return null;
    }

    /**
     * Parses the JWT string and returns its claims set.
     * This method is specifically for blacklist logic: it tries to parse the token
     * to get JTI and expiration time, even if the token might be expired or
     * its signature isn't re-verified here (assuming filter did initial validation).
     * It only fails if the token is malformed and cannot be parsed.
     *
     * @param token the JWT string
     * @return JWTClaimsSet if parsable, otherwise null.
     */
    public JWTClaimsSet getClaimsToParseForBlacklist(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            // For blacklisting, we primarily need the JTI and original expiration.
            // We don't re-verify signature here, assuming the initial request
            // already went through the JwtAuthenticationFilter which does verification.
            // If logout is called with a totally bogus token, parsing might fail.
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            log.error("Cannot parse token for blacklist purposes (malformed token): {}. Token: {}...", e.getMessage(), token.length() > 10 ? token.substring(0, 10) : token);
            return null;
        }
    }
}