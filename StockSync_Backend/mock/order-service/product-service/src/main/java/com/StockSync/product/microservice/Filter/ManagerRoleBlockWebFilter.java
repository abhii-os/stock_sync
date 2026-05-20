package com.StockSync.product.microservice.Filter;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class ManagerRoleBlockWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String roles = exchange.getRequest().getHeaders().getFirst("X-Auth-User-Roles");
        String path = exchange.getRequest().getURI().getPath();

        System.out.println("ManagerRoleBlockWebFilter triggered for path: " + path + ", roles: " + roles);

        String cleaned = null;
        if (roles != null && !roles.isBlank()) {
            cleaned = roles.replace("\"", "").replace("[", "").replace("]", "");
        }

        boolean hasManager = false;
        if (cleaned != null) {
            hasManager = Arrays.stream(cleaned.split("[,;\\s]+"))
                    .map(String::trim)
                    .anyMatch(r -> r.equalsIgnoreCase("ROLE_MANAGER") || r.equalsIgnoreCase("MANAGER"));
        }

        boolean isCategoryPath = path != null && (path.equals("/api/v1/categories") || path.startsWith("/api/v1/categories") || path.contains("/categories"));

        if (isCategoryPath && hasManager) {
            System.out.println("Blocking MANAGER access to category endpoint (webfilter)");
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            byte[] bytes = "Access denied for MANAGER role".getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        }

        return chain.filter(exchange);
    }
}
