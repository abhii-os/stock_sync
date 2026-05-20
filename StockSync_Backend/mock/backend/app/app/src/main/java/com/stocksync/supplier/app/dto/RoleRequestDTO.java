package com.stocksync.supplier.app.dto;

import com.stocksync.supplier.app.enums.RoleType;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;



@Data
@Builder
public class RoleRequestDTO {
   @NotNull(message = "Role name is required")
    private RoleType name;
    private String description;
}