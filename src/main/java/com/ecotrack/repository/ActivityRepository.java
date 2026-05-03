package com.ecotrack.repository;


import com.ecotrack.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    List<Activity> findByUserIdOrderByActivityDateDesc(UUID userId);
    long countByUserId(UUID userId);
    long countByUserIdAndActivityCategory(UUID userId, String category);
}