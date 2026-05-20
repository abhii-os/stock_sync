package com.stocksync.userservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.stocksync.userservice.dto.RoleRequestDTO;
import com.stocksync.userservice.dto.UserRegistrationRequestDTO;
import com.stocksync.userservice.entity.Role;
import com.stocksync.userservice.enums.RoleType;
import com.stocksync.userservice.service.api.RoleService;
import com.stocksync.userservice.service.api.UserMgmtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedDataLoader implements CommandLineRunner {

    private final UserMgmtService userMgmtService;
    private final RoleService roleService;

    @Override
    public void run(String... args) throws Exception {
        // Create or fetch roles
        Role adminRole = roleService.seedRoleData(RoleRequestDTO.builder()
                .name(RoleType.ROLE_ADMIN)
                .description("Admin User Role")
                .build());

        Role userRole = roleService.seedRoleData(RoleRequestDTO.builder()
                .name(RoleType.ROLE_MANAGER)
                .description("Manager Role")
                .build());

        // Create admin user
        if (userMgmtService.isNewUser("admin@gmail.com")) {
            UserRegistrationRequestDTO userReqDTO = new UserRegistrationRequestDTO("admin@gmail.com", "abhi@123",
                    "admin@ims.com", "ADMIN", " ",
                    "987654321", RoleType.ROLE_ADMIN);
            userMgmtService.registerNewUser(userReqDTO);
        }


    }
}

