package com.stocksync.userservice.resource;

import com.stocksync.userservice.dto.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stocksync-userservice/users")
public class UserController {

    /**
     * Gets the details of the currently authenticated user.
     * This endpoint is protected by your SecurityConfig.
     */
    @GetMapping("/current")
    public ResponseEntity<?> getLoggedInUserInfo() {

        // 1. Get the authentication object from Spring Security's context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Check if the user is actually authenticated
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body("No user is authenticated.");
        }

        // 3. Get the 'Principal' (the user object)
        Object principal = authentication.getPrincipal();

        // 4. Check if it's the CustomUserDetails object you created
        if (principal instanceof CustomUserDetails) {
            // Cast it and return it. Spring will turn this into JSON.
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            return ResponseEntity.ok(userDetails);
        }

        // 5. This is a fallback in case the principal is not what we expect
        return ResponseEntity.status(500).body("Could not determine user details from principal.");
    }
}