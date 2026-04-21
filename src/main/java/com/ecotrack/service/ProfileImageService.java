package com.ecotrack.service;

import com.ecotrack.entity.ProfileImage;
import com.ecotrack.entity.User;
import com.ecotrack.repository.ProfileImageRepository;
import com.ecotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final ProfileImageRepository mongoRepository;
    private final UserRepository postgresRepository;
    public String saveImage(String email, String base64Image , String contentType) {
        User user = postgresRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("User not found"));

        ProfileImage image = mongoRepository.findByUserId(user.getId().toString())
                .orElse(new ProfileImage());

        image.setUserId(user.getId().toString());
        image.setImageBase64(base64Image);
        image.setContentType(contentType);
        mongoRepository.save(image);
        return "Image blasted to Mongodb Atlas";
    }

    public ProfileImage getImage(String email) {
        User user = postgresRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("user not found"));
        return mongoRepository.findByUserId(user.getId().toString()).orElse(null);
    }


}
