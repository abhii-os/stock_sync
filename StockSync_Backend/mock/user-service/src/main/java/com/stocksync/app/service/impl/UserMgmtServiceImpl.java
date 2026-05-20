package com.stocksync.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.stocksync.userservice.dto.UserRegistrationRequestDTO;
import com.stocksync.userservice.dto.UserRegistrationResponseDTO;
import com.stocksync.userservice.entity.Role;
import com.stocksync.userservice.entity.User;
import com.stocksync.userservice.enums.RoleType;
import com.stocksync.userservice.exception.UserAlreadyExistsException;
import com.stocksync.userservice.repo.UserRepository;
import com.stocksync.userservice.service.api.RoleService;
import com.stocksync.userservice.service.api.UserMgmtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserMgmtServiceImpl implements UserMgmtService {

    private  final UserRepository userRepository;

    private final RoleService roleService;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserRegistrationResponseDTO registerNewUser(UserRegistrationRequestDTO userReqDTO) {
       Optional<User> existingUser = userRepository.findByUsername(userReqDTO.getUsername());
        User persUser = null;
        if(existingUser.isPresent() && isUserRoleExists(existingUser.get(),userReqDTO.getRoleType())) {
            log.error(" {}  already exists ", userReqDTO.getUsername());
            throw new UserAlreadyExistsException(userReqDTO.getUsername() +" already exists");
        }
        else if(existingUser.isPresent()) {
            User userEntity =  existingUser.get();
            userEntity.getRoles().add(roleService.getRoleByName(userReqDTO.getRoleType()));
            persUser = userRepository.save(userEntity);
            log.info("New  role {} added for  existing {}",userReqDTO.getRoleType(), existingUser.get().getFirstName());
        }
        else {
            User user = User.builder().username(userReqDTO.getUsername()).password(passwordEncoder.encode(userReqDTO.getPassword()))
                    .firstName(userReqDTO.getFirstName()).lastName(userReqDTO.getLastName()).phone(userReqDTO.getPhone())
                    .email(userReqDTO.getEmail())
                    .roles(Set.of(roleService.getRoleByName(userReqDTO.getRoleType())))
                    .build();
            persUser = userRepository.save(user);
        }
       //convert persUser  to UserRegistrationResponseDTO  and return
        StringBuilder welcomeMesaage = new StringBuilder(" Welcome ");
        welcomeMesaage.append(persUser.getFirstName()).append(" ").append(persUser.getLastName())
                .append("  to Genc  App");
        return UserRegistrationResponseDTO.builder()
                .id(persUser.getId())
                .username(persUser.getUsername())
                .firstName(persUser.getFirstName())
                .lastName(persUser.getLastName())
                .phone(persUser.getPhone())
                .email(persUser.getEmail())
                .roles(persUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .userMessage(welcomeMesaage.toString())
                .build();
    }

    private boolean isUserRoleExists(User userObj, RoleType newRole) {
        boolean roleExists = userObj.getRoles().stream().anyMatch( r -> r.getName().equals(newRole));
        log.info("roleExists {}",roleExists);
        return  roleExists;
    }

    @Override
    public boolean isNewUser(String userName) {
        Optional<User> userObj =  userRepository.findByUsername(userName);
        log.info("if User doesn't exists {} ", userObj.isEmpty());
        return userObj.isEmpty();
    }
}
