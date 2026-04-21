package com.ecotrack.controller;

import com.ecotrack.dto.UserProfileRequest;
import com.ecotrack.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping
    public ResponseEntity<String> saveProfile(@RequestBody UserProfileRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        String result = userProfileService.saveProfile(email,request);

        return ResponseEntity.ok(result);

    }

}
