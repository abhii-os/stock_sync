package com.stocksync.userservice.resource;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.stocksync.userservice.dto.ErrorResponse;
import com.stocksync.userservice.dto.RoleRequestDTO;
import com.stocksync.userservice.dto.RoleResponseDTO;
import com.stocksync.userservice.service.api.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stocksync-userservice/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Create new role")
    public ResponseEntity<?> createRole(
            @Validated @RequestBody RoleRequestDTO request,
            HttpServletRequest servletRequest) {
        try {
            RoleResponseDTO response = roleService.createRole(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<?> getRole(@PathVariable Long id, HttpServletRequest servletRequest) {
        try {
            RoleResponseDTO response = roleService.getRoleById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    @GetMapping
    @Operation(summary = "Get all roles")
    public ResponseEntity<List<RoleResponseDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role")
    public ResponseEntity<?> updateRole(
            @PathVariable Long id,
            @Validated @RequestBody RoleRequestDTO request,
            HttpServletRequest servletRequest) {
        try {
            RoleResponseDTO response = roleService.updateRole(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> deleteRole(@PathVariable Long id, HttpServletRequest servletRequest) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        }
    }
}
