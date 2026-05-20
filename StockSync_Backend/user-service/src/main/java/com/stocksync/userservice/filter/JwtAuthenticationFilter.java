package com.stocksync.userservice.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.stocksync.userservice.service.impl.CustomUserDetailsService;
import com.stocksync.userservice.util.JwtUtil;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final CustomUserDetailsService userDetailsService;


    // Define all paths that should NOT be filtered (i.e., public paths)
    // NOTE: BASE_SERVICE_PATH should be accessible or hardcoded here if needed
    private static final List<String> SKIPPED_PATHS = List.of(
            "/api/v1/stocksync-userservice/login",
            "/api/v1/stocksync-userservice/register", // registration path
            "/actuator",
            "/v3/api-docs",
            "/swagger-ui"
            // Add any other public paths here
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Check if the current request path starts with any of the skipped paths
        String path = request.getRequestURI();

        // Match only POST requests for /login and /users (registration)
        if (request.getMethod().equals(HttpMethod.POST.name())) {
            if (path.startsWith("/api/v1/stocksync-userservice/login") || path.startsWith("/api/v1/stocksync-userservice/register")) {
                return true;
            }
        }

        // Match all other defined skipped paths regardless of HTTP method (e.g., swagger)
        return SKIPPED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                log.error("Error extracting username from token: {}", e.getMessage());
            }
        } else {
            log.debug("No Authorization header or invalid format for request: {}", request.getRequestURI());
        }

        // Validate token and set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtUtil.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Authenticated user: {} for request: {}", username, request.getRequestURI());
            } else {
                log.warn("Invalid JWT token for user: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}