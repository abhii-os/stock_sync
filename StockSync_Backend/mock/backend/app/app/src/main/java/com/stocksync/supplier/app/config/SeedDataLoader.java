package com.stocksync.supplier.app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.stocksync.supplier.app.dto.RoleRequestDTO;
import com.stocksync.supplier.app.dto.UserRegistrationRequestDTO;
import com.stocksync.supplier.app.entity.Role;
import com.stocksync.supplier.app.enums.RoleType;
import com.stocksync.supplier.app.service.api.RoleService;
import com.stocksync.supplier.app.service.api.UserMgmtService;

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
            UserRegistrationRequestDTO userReqDTO = new UserRegistrationRequestDTO("admin@gmail.com", "dwivedi@123",
                    "admin@ims.com", "ADMIN", "title",
                    "987654321", RoleType.ROLE_ADMIN);
            userMgmtService.registerNewUser(userReqDTO);
        }

        // // Create regular user
        // if (userMgmtService.isNewUser("manager@ims.com")) {
        //     UserRegistrationRequestDTO userReqDTO = new UserRegistrationRequestDTO("manager@ims.com", "user123",
        //             "manager@ims.com", "user1", "title",
        //             "6657932766", RoleType.ROLE_MANAGER);
        //     userMgmtService.registerNewUser(userReqDTO);
        // }
    }
}

