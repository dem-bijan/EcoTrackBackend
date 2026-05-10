package com.ecotrack.service;


import com.ecotrack.entity.Activity;
import com.ecotrack.entity.User;
import com.ecotrack.repository.ActivityRepository;
import com.ecotrack.repository.UserAchievementRepository;
import com.ecotrack.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    @Transactional
    public Activity logActivity(String email,Activity activity) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        activity.setUser(user);

        Activity saved= activityRepository.save(activity);


        return saved;
    }


    public List<Activity> getUserActivities(UUID userId){
        return activityRepository.findByUserIdOrderByActivityDateDesc(userId);
    }

    public List<Activity> getUserActivities(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return activityRepository.findByUserIdOrderByActivityDateDesc(user.getId());
    }

}
