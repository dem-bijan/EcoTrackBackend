package com.ecotrack.controller;

import com.ecotrack.dto.ProfileImageRequest;
import com.ecotrack.entity.ProfileImage;
import com.ecotrack.service.ProfileImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    @PostMapping
    public ResponseEntity<String> uploadImage(@RequestBody ProfileImageRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String result = profileImageService.saveImage(email, request.getBase64Image() , request.getContentType());
        return ResponseEntity.ok(result);
    }
    @GetMapping
    public ResponseEntity<ProfileImageRequest> getImage() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ProfileImage image = profileImageService.getImage(email);
        if(image == null) {
            return ResponseEntity.notFound().build();
        }
        ProfileImageRequest response = new ProfileImageRequest();
        response.setBase64Image(image.getImageBase64());
        response.setContentType(image.getContentType());
        return  ResponseEntity.ok(response);
    }
}
