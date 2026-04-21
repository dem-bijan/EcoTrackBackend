package com.ecotrack.service;

import com.ecotrack.dto.UserProfileRequest;
import com.ecotrack.entity.User;
import com.ecotrack.entity.UserProfile;
import com.ecotrack.repository.UserProfileRepository;
import com.ecotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public  String saveProfile(String email , UserProfileRequest request) {
        User user = userRepository.findByEmail(email)
                    .orElseThrow(()-> new RuntimeException("User not Found"));

        if(request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }

        Optional<UserProfile> existingProfile = userProfileRepository.findByUserId(user.getId());
        UserProfile profile;
        if (existingProfile.isPresent()){
            profile = existingProfile.get();
        }
        else {
            profile = new UserProfile();
            profile.setUser(user);
        }
        profile.setHousingType(request.getHousingType());
        profile.setHouseholdSize(request.getHouseholdSize());
        profile.setDietType(request.getDietType());
        profile.setVehicleType(request.getVehicleType());
        profile.setAnnualMileage(request.getAnnualMileage());
        profile.setEnergySource(request.getEnergySource());
        profile.setPreferences(request.getPreferences());
        userProfileRepository.save(profile);
        return "Profile data securely saved!";
    }
}
