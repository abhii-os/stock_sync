package org.infra.genc.apigateway.filter;

import org.infra.genc.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    // These constants are used for injecting trusted headers
    public static final String AUTH_USER_ID_HEADER = "X-Auth-User-Id";
    public static final String AUTH_USER_ROLES_HEADER = "X-Auth-User-Roles";

    private final RouteValidator validator;
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(RouteValidator validator, JwtUtil jwtUtil) {
        super(Config.class);
        this.validator = validator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // 1. Check if the route is marked as secured by the RouteValidator
            if (validator.isSecured.test(request)) {

                var headers = request.getHeaders();
                List<String> authValues = headers.getOrEmpty(HttpHeaders.AUTHORIZATION);

                if (authValues.isEmpty()) {
                    log.warn("Missing Authorization header for request {}", request.getURI());
                    return setUnauthorizedResponse(response);
                }

                String authHeader = authValues.get(0);
                if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                    log.warn("Invalid Authorization header format for request {}", request.getURI());
                    return setUnauthorizedResponse(response);
                }

                String token = authHeader.substring(7);
                try {
                    // 2. Validate Token and Extract Claims
                    jwtUtil.validateToken(token);
                    Claims claims = jwtUtil.extractAllClaims(token);

                    String userId = claims.getSubject();

                    // --- ROLE EXTRACTION AND NORMALIZATION ---
                    Object rolesClaim = claims.get("roles");
                    String userRoles;

                    if (rolesClaim instanceof List) {
                        // If it comes as a list (JSON array in JWT), join them with commas.
                        userRoles = String.join((CharSequence) ",", (CharSequence) rolesClaim);
                    } else {
                        // Otherwise, treat it as a string (handling null safely)
                        userRoles = Optional.ofNullable(claims.get("roles", String.class)).orElse("");
                    }

                    String path = request.getURI().getPath();
                    HttpMethod method = request.getMethod();

                    // CRITICAL DEBUGGING LOGS: CHECK THESE IN YOUR CONSOLE!
                    log.info("--- AUTH DEBUG ---");
                    log.info("Raw Role Claim Object Type: {}", rolesClaim != null ? rolesClaim.getClass().getSimpleName() : "null");
                    log.info("Final Processed Roles String: '{}'", userRoles);
                    log.info("Request Path Being Checked: '{}'", path);
                    log.info("Request Method: {}", method);
                    log.info("Target Role for Restriction: ROLE_MANAGER");
                    log.info("------------------");

                    // --- Authorization Check ---
                    if (isPathRestricted(path, userRoles, method)) {
                        log.warn("Access denied [403 Forbidden] for user {} with roles [{}] on path {} {}", userId, userRoles, method, path);
                        response.setStatusCode(HttpStatus.FORBIDDEN);
                        return response.setComplete();
                    }


                    // 3. Inject Trusted Headers for Downstream Services
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header(AUTH_USER_ID_HEADER, userId)
                            .header(AUTH_USER_ROLES_HEADER, userRoles.isEmpty() ? "GUEST" : userRoles)
                            .build();

                    // Replace the original exchange with the modified request
                    exchange = exchange.mutate().request(modifiedRequest).build();

                } catch (Exception e) {
                    log.warn("Token validation failed for request {}: {}", request.getURI(), e.getMessage());
                    return setUnauthorizedResponse(response);
                }
            }

            log.debug("Authentication/Authorization Context passed for {}", request.getURI());
            return chain.filter(exchange);
        };
    }

    /**
     * Checks if the request path requires a restriction based on the user's role AND HTTP method.
     * NEW POLICY: DENY access if the path is restricted AND the request is a mutation (POST/PUT/DELETE)
     * AND the user only has ROLE_MANAGER (i.e., we are reserving mutation access for ROLE_ADMIN).
     */
    private boolean isPathRestricted(String path, String userRoles, HttpMethod method) {
        // 1. Define the paths that trigger the restriction
        boolean isRestrictedPath = path.startsWith("/api/suppliers") || path.startsWith("/api/v1/categories");

        // 2. Check if the request is a MUTATING (Write) request
        boolean isMutation = method.matches(HttpMethod.POST.toString()) ||
                method.matches(HttpMethod.PUT.toString()) ||
                method.matches(HttpMethod.DELETE.toString());

        // 3. Check if the user has the ROLE_MANAGER role (but not a higher one, like ROLE_ADMIN, which we assume should be allowed)
        // For simplicity, we check if they are a manager. If the user has both ROLE_MANAGER and ROLE_ADMIN, they will pass.
        boolean isManager = hasRole(userRoles, "ROLE_MANAGER");

        // The manager is blocked ONLY if they hit a restricted path AND it's a mutation request.
        // If it's a GET request, this returns false, allowing them to read the data.
        return isRestrictedPath && isMutation && isManager;
    }

    /**
     * Robustly checks if the userRoles string contains the required role, handling various delimiters.
     */
    private boolean hasRole(String userRoles, String targetRole) {
        if (!StringUtils.hasText(userRoles)) {
            return false;
        }

        // Replace common delimiters (comma, space) with a single space, then split, and check for a match
        String normalizedRoles = userRoles.replace(',', ' ');
        return Arrays.stream(normalizedRoles.split("\\s+"))
                .anyMatch(role -> role.trim().equalsIgnoreCase(targetRole));
    }


    private reactor.core.publisher.Mono<Void> setUnauthorizedResponse(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    public static class Config {
        // Configuration placeholder
    }
}