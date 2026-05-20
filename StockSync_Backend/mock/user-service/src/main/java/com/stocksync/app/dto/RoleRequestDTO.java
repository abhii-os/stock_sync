package com.stocksync.userservice.dto;

import com.stocksync.userservice.enums.RoleType;

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