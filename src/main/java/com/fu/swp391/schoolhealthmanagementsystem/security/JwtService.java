package com.fu.swp391.schoolhealthmanagementsystem.security;

import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.prop.JwtProperties;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwt;


    public String generateToken(User user) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            JWSSigner signer = new MACSigner(jwt.secret().getBytes(StandardCharsets.UTF_8));
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwt.expirationMs());

            List<String> authorities = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUserId().toString())
                    .issuer(jwt.issuer())
                    .issueTime(now)
                    .expirationTime(expiryDate)
                    .jwtID(UUID.randomUUID().toString()) // JTI
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
            JWSVerifier verifier = new MACVerifier(jwt.secret().getBytes(StandardCharsets.UTF_8));
            if (!signedJWT.verify(verifier)) {
                log.warn("JWT signature validation failed for token: {}...", token.length() > 7 ? token.substring(0,7) : token);
                return false;
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet(); // Get claims AFTER verification
            if (claimsSet.getExpirationTime().before(new Date())) {
                log.warn("JWT token has expired. Token: {}...", token.length() > 7 ? token.substring(0,7) : token);
                return false;
            }
            if (!jwt.issuer().equals(claimsSet.getIssuer())) {
                log.warn("JWT issuer mismatch. Expected: {}, Actual: {}. Token: {}...", jwt.issuer(), claimsSet.getIssuer(), token.length() > 7 ? token.substring(0,7) : token);
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

    public JWTClaimsSet getClaimsFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            // SỬA Ở ĐÂY: Đảm bảo dùng UTF-8
            JWSVerifier verifier = new MACVerifier(jwt.secret().getBytes(StandardCharsets.UTF_8));
            if (signedJWT.verify(verifier)) {
                JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
                if (claimsSet.getExpirationTime().before(new Date())) {
                    log.warn("Attempted to get claims from an expired token (verified signature but expired). Token: {}...", token.length() > 7 ? token.substring(0,7) : token);
                    return null;
                }
                if (!jwt.issuer().equals(claimsSet.getIssuer())) {
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


    public JWTClaimsSet getClaimsToParseForBlacklist(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            log.error("Cannot parse token for blacklist purposes (malformed token): {}. Token: {}...", e.getMessage(), token.length() > 10 ? token.substring(0, 10) : token);
            return null;
        }
    }
}