package com.stocksync.userservice.service.impl;

import com.stocksync.userservice.dto.CustomUserDetails;
import com.stocksync.userservice.entity.User;
import com.stocksync.userservice.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        log.info("User   found username {} , role s ize {}" , user.getUsername(),user.getRoles().size());
      //  user.getRoles().forEach(r-> log.info("Role is {} ", r.getName()));
       /* AbstractUserDetailsAuthenticationProvider.authenticate -->
       DaoAuthenticationProvider.additionalAuthenticationChecks  requires password*/
         new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName().toString()))
                        .collect(Collectors.toList())
        );

         return new CustomUserDetails(user);
    }
}