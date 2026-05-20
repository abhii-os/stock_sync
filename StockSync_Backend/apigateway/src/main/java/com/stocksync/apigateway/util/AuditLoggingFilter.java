package com.stocksync.apigateway.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * A single Global Filter to handle comprehensive audit logging for
 * both incoming API requests and outgoing API responses, including latency.
 * * This filter runs at a high precedence to wrap the entire execution chain.
 */
@Component
public class AuditLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggingFilter.class);

    // Key used to store the request start time in the ServerWebExchange attributes.
    private static final String START_TIME = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // --- 1. PRE-ROUTE LOGGING (Request Phase) ---

        // Store the start time to calculate latency later
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());

        final String requestId = exchange.getRequest().getId();
        final String method = exchange.getRequest().getMethod().name();
        final String uri = exchange.getRequest().getURI().getPath();
        final String target = exchange.getRequest().getHeaders().getFirst("Host");

        // Log the incoming request
        log.info(" REQUEST [ID: {}] | {} {} | Target: {}",
                requestId, method, uri, target);


        // --- 2. CHAIN EXECUTION AND POST-ROUTE LOGGING (Response Phase) ---

        // Proceed with the rest of the filter chain (which calls the downstream service)
        return chain.filter(exchange)
                // .then() executes the code AFTER the downstream service has returned
                // and the filter chain is completing.
                .then(Mono.fromRunnable(() -> {

                    // Retrieve the execution time and status from the exchange
                    Long startTime = exchange.getAttribute(START_TIME);
                    long duration = startTime != null ? (System.currentTimeMillis() - startTime) : -1;

                    // Safely get the status code, defaulting to 500 if null (e.g., error before response was set)
                    int status = exchange.getResponse().getStatusCode() != null ?
                            exchange.getResponse().getStatusCode().value() : 500;

                    // Log the outgoing response
                    log.info(" RESPONSE [ID: {}] | Status: {} | Latency: {}ms",
                            requestId, status, duration);

                }));
    }

    /**
     * Set a high order to ensure this filter runs very early (for request logging)
     * and wraps the entire execution (for response logging).
     * @return The filter order.
     */
    @Override
    public int getOrder() {
        // A high precedence value to ensure it runs before most other filters (like routing)
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
