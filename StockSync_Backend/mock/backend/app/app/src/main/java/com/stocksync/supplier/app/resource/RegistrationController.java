package com.stocksync.supplier.app.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.stocksync.supplier.app.dto.ErrorResponse;
import com.stocksync.supplier.app.dto.UserRegistrationRequestDTO;
import com.stocksync.supplier.app.dto.UserRegistrationResponseDTO;
import com.stocksync.supplier.app.exception.UserAlreadyExistsException;
import com.stocksync.supplier.app.service.api.UserMgmtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/stocksync-userservice")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final UserMgmtService userMgmtService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    @ApiResponse(responseCode = "201", description = "User successfully registered")
    public ResponseEntity<?> registerUser(
            @Validated @RequestBody UserRegistrationRequestDTO request,
            HttpServletRequest servletRequest) {

        log.info("Registering new user with username: {}", request.getUsername());

        try {
            UserRegistrationResponseDTO response = userMgmtService.registerNewUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.of(
                            HttpStatus.CONFLICT,
                            "User already exists with username: " + request.getUsername(),
                            servletRequest.getRequestURI()
                    ));
        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error registering user: " + e.getMessage(),
                            servletRequest.getRequestURI()
                    ));
        }
    }
}
