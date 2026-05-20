package com.stocksync.userservice.dto;

import com.stocksync.userservice.enums.RoleType;
import lombok.Builder;
import lombok.Data;


import java.util.Set;

@Data
@Builder
public class UserRegistrationResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String userMessage;
    private Set<RoleType> roles;
}
