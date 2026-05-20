package com.StockSync.product.microservice.Filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

public class ManagerRoleBlockFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String roles = httpRequest.getHeader("X-Auth-User-Roles");
        String path = httpRequest.getRequestURI();

        System.out.println("ManagerRoleBlockFilter triggered for path: " + path + ", roles: " + roles);

        boolean hasManager = false;
        if (roles != null && !roles.isBlank()) {
            // remove common wrappers ([ ], quotes) and split on commas/whitespace
            String cleaned = roles.replace("\"", "").replace("[", "").replace("]", "");
            hasManager = Arrays.stream(cleaned.split("[,;\\s]+"))
                    .map(String::trim)
                    .anyMatch(r -> r.equalsIgnoreCase("ROLE_MANAGER") || r.equalsIgnoreCase("MANAGER"));
        }

        // Match common variations the gateway or client might send
        boolean isCategoryPath = path.startsWith("/api/v1/categories") || path.startsWith("/api/categories") || path.contains("/categories");

        if (isCategoryPath && hasManager) {
            System.out.println("Blocking MANAGER access to category endpoint (robust match)");
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().write("Access denied for MANAGER role");
            return;
        }

        chain.doFilter(request, response);
    }
}