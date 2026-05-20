package com.stocksync.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String jwt;
    private Long id;
    private String firstName;
    private String lastName;
    private String role;
    private String email;
    private String phone;
    private String appInstance;

}
