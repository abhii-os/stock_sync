package com.stocksync.apigateway.filter;

import com.stocksync.apigateway.util.JwtUtil;
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


                    Object rolesClaim = claims.get("roles");
                    String userRoles;

                    if (rolesClaim instanceof List) {

                        userRoles = String.join((CharSequence) ",", (CharSequence) rolesClaim);
                    } else {

                        userRoles = Optional.ofNullable(claims.get("roles", String.class)).orElse("");
                    }

                    String path = request.getURI().getPath();
                    HttpMethod method = request.getMethod();


                    log.info("--- AUTH DEBUG ---");
                    log.info("Raw Role Claim Object Type: {}", rolesClaim != null ? rolesClaim.getClass().getSimpleName() : "null");
                    log.info("Final Processed Roles String: '{}'", userRoles);
                    log.info("Request Path Being Checked: '{}'", path);
                    log.info("Request Method: {}", method);
                    log.info("Target Role for Restriction: ROLE_MANAGER");
                    log.info("------------------");


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


    private boolean isPathRestricted(String path, String userRoles, HttpMethod method) {

        boolean isRestrictedPath = path.startsWith("/api/suppliers") || path.startsWith("/api/v1/categories");

        boolean isMutation = method.matches(HttpMethod.POST.toString()) ||
                method.matches(HttpMethod.PUT.toString()) ||
                method.matches(HttpMethod.DELETE.toString());

        boolean isManager = hasRole(userRoles, "ROLE_MANAGER");

        return isRestrictedPath && isMutation && isManager;
    }

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

    }
}