package com.ecotrack.controller;

import com.ecotrack.dto.UpdateEmailRequest;
import com.ecotrack.dto.UpdateNameRequest;
import com.ecotrack.service.UserModifService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.ecotrack.entity.User;
import com.ecotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.ecotrack.dto.UpdatePasswordRequest;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserModifService userModifService ;

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
    @PutMapping("/email")
    public ResponseEntity<?> changeEmail(@RequestBody UpdateEmailRequest request) {
        String oldEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        userModifService.updateEmail(oldEmail,request);
        return ResponseEntity.ok("Email updated , Please re-verify your new adress");
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePssword(@RequestBody UpdatePasswordRequest request) {
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        userModifService.updatePassword(email,request);

        return ResponseEntity.ok("Password Updated successfully");
    }

    @PutMapping("/name")
    public ResponseEntity<?> changeName(@RequestBody UpdateNameRequest request) {
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        userModifService.updateName(email,request);
        return ResponseEntity.ok("Name Changed successfully");

    }
}
