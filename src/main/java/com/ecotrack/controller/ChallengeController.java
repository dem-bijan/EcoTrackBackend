package com.ecotrack.controller;

import com.ecotrack.entity.User;
import com.ecotrack.entity.UserChallenge;
import com.ecotrack.repository.UserChallengeRepository;
import com.ecotrack.repository.UserRepository;
import com.ecotrack.service.ChallengeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {
    private final ChallengeService challengeService;
    private final UserChallengeRepository userChallengeRepository;
    private final UserRepository userRepository;

    public ChallengeController(ChallengeService challengeService, UserChallengeRepository userChallengeRepository, UserRepository userRepository) {
        this.challengeService = challengeService;
        this.userChallengeRepository = userChallengeRepository;
        this.userRepository = userRepository;
    }


    @GetMapping("/me")
    public ResponseEntity<List<UserChallenge>> getUserChallenges() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userChallengeRepository.findByUserEmailAndStatusIn(email,List.of("ACTIVE","PROPOSED")));
    }

    @PostMapping("/accept/{code}")
    public ResponseEntity<?> accept(@PathVariable String code) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        challengeService.startChallenge(code, email);
        return ResponseEntity.ok().build();
    }

}
