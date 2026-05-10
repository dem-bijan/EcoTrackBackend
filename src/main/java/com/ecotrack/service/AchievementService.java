package com.ecotrack.service;

import com.ecotrack.entity.Achievement;
import com.ecotrack.entity.User;
import com.ecotrack.entity.UserAchievement;
import com.ecotrack.repository.AchievementRepository;
import com.ecotrack.repository.ActivityRepository;
import com.ecotrack.repository.UserAchievementRepository;
import com.ecotrack.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AchievementService(AchievementRepository achievementRepository, UserAchievementRepository userAchievementRepository, ActivityRepository activityRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void seedAchievements() {
        if (achievementRepository.count() == 0) {
            createAchievement("FIRST_LOG", "First Step", "Logged your very first eco-friendly activity.", 50);
            createAchievement("FIVE_LOGS", "Consistency Key", "Logged 5 eco-friendly activities.", 150);
            createAchievement("TEN_LOGS", "Eco Warrior", "Logged 10 eco-friendly activities.", 500);
            createAchievement("VEGAN_DAY", "Plant Powered", "Logged a vegan meal substitution.", 100);
        }
    }

    private void createAchievement(String code, String title, String description, int points) {
        Achievement ach = new Achievement();
        ach.setCode(code);
        ach.setTitle(title);
        ach.setDescription(description);
        ach.setPoints(points);
        achievementRepository.save(ach);
    }
    public void checkAchievements(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        long totalActivities = activityRepository.countByUserId(user.getId());
        boolean unlockedNew = false;
        if (totalActivities >= 1) unlockedNew |= awardAchievement(user, "FIRST_LOG");
        if (totalActivities >= 5) unlockedNew |= awardAchievement(user, "FIVE_LOGS");
        if (totalActivities >= 10) unlockedNew |= awardAchievement(user, "TEN_LOGS");
        if (unlockedNew) {
            messagingTemplate.convertAndSend("/topic/updates/" + email, "REFETCH_ACHS");
        }
    }
    private boolean awardAchievement(User user, String code) {
        if (!userAchievementRepository.existsByUserEmailAndAchievementCode(user.getEmail(), code)) {
            Achievement ach = achievementRepository.findByCode(code).orElse(null);
            if (ach != null) {
                UserAchievement ua = new UserAchievement();
                ua.setUser(user);
                ua.setAchievement(ach);
                userAchievementRepository.save(ua);
                return true;
            }
        }
        return false;
    }

}


