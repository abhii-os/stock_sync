package com.stocksync.userservice.dto;

import com.stocksync.userservice.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class UserRegistrationRequestDTO {

    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private RoleType roleType;
}
