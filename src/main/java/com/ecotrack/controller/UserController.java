package com.ecotrack.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ecotrack.entity.User;
import com.ecotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    // This securely returns the User based ONLY on their encrypted JWT badge!
    @GetMapping("/me")
    public ResponseEntity<User> getMyInformation() {
        // Extract the email from the JWT
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Find them in Neon Postgres
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }
}
