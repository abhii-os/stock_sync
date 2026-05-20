package com.stocksync.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.stocksync.userservice.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles security errors by returning a 401 response with a custom JSON body.
 * This is triggered by the ExceptionTranslationFilter when an AuthenticationException occurs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // 💡 FIX: Inject the Spring-managed ObjectMapper, which is correctly configured
    // to handle Java 8 date/time types (like LocalDateTime).
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // Log the authentication error
       log.error("Authentication failure: {} " , authException.getMessage());

        // 1. Set the correct status code and content type
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 2. Create the custom error object
        // NOTE: Ensure your ErrorResponse.of(...) correctly creates a timestamp field.
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED,
                authException.getMessage(),
                request.getRequestURI()
        );

        // 3. Write the JSON body to the response output stream using the configured ObjectMapper
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
