package com.ecotrack.repository;

import com.ecotrack.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    List<UserAchievement> findByUserEmail(String email);
    boolean existsByUserEmailAndAchievementCode(String email, String code);
    int countByUserId(UUID userId);
}
