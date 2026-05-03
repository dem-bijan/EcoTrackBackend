package com.ecotrack.service;

import com.ecotrack.entity.Recommendation;
import com.ecotrack.entity.User;
import com.ecotrack.repository.RecommendationRepository;
import com.ecotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final UserRepository userRepository;
    private final RecommendationRepository recommendationRepository;

    public List<Recommendation> getUserRecommendations(UUID userId) {
        return recommendationRepository.findByUser_IdAndStatus(userId, "pending");
    }


    @Transactional
    public void saveRecomendation(String email, Recommendation rec){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));

        rec.setUser(user);
        var exist = recommendationRepository.findByUser_IdAndStatus(user.getId(),"pending")
                .stream()
                .filter(r-> r.getTitle().equalsIgnoreCase(rec.getTitle()))
                .findFirst();

        if(exist.isPresent()){
            Recommendation found = exist.get();
            // Increment priority score if the AI suggests the same improvement again
            found.setPriorityScore(found.getPriorityScore() + 3);
            recommendationRepository.save(found);
        }
        else {
            rec.setUser(user);
            if(rec.getPriorityScore() == null) rec.setPriorityScore(3);
            recommendationRepository.save(rec);
        }
    }
}
