package com.stocksync.userservice.util;

import com.stocksync.userservice.dto.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtil {
    @Value("${genc.jwt.secret}")
    private String secret;

    @Value("${genc.jwt.expiration:900000}")
    private Long expiration;

    // Generate a SecretKey from the secret string
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        log.info("isTokenExpired {} ", extractExpiration(token).before(new Date()));
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        return createToken(claims, userDetails);
    }

    private String createToken(Map<String, Object> claims, CustomUserDetails userDetails) {
        claims.put("roles", extractAndConcatenateRoles(userDetails));
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .issuer("genc_cohort")
                .audience().add("GenC").and()
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error(" Exception in validateToken {} with message ", e.getCause(), e.getMessage());
            return false;
        }
    }

    public String extractAndConcatenateRoles(CustomUserDetails userDetails) {
        Set<? extends GrantedAuthority> authorities = (Set<? extends GrantedAuthority>) userDetails.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return ""; // Return empty string if no roles are found
        }
        String rolesString = authorities.stream()
                .map(GrantedAuthority::getAuthority) // Extracts the String role name (e.g., "ROLE_ADMIN")
                .collect(Collectors.joining(","));
        return rolesString;
    }
}