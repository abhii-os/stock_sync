package com.stocksync.supplier.app.dto;

import com.stocksync.supplier.app.enums.RoleType;
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
