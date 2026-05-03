package com.ecotrack.controller;

import com.ecotrack.entity.Recommendation;
import com.ecotrack.service.RecommendationService;
import com.ecotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Recommendation>> getMyRecommendations() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user =  userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("no user found"));
        List<Recommendation> recs = recommendationService.getUserRecommendations(user.getId());
        return ResponseEntity.ok(recs);

    }

}
