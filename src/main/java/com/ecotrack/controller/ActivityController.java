package com.ecotrack.controller;
import com.ecotrack.entity.Activity;
import com.ecotrack.repository.UserRepository;
import com.ecotrack.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/activities")
@CrossOrigin(origins = "http://localhost:3000")
public class ActivityController {
    private final ActivityService activityService;

    @GetMapping("/me")
    public ResponseEntity<List<Activity>> getMyActivities() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Activity> activities = activityService.getUserActivities(email);
        return ResponseEntity.ok(activities);
    }
}
