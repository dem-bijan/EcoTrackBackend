package com.ecotrack.service;

import com.ecotrack.entity.Activity;
import com.ecotrack.entity.Challenge;
import com.ecotrack.entity.User;
import com.ecotrack.entity.UserChallenge;
import com.ecotrack.repository.ChallengeRepository;
import com.ecotrack.repository.UserChallengeRepository;
import com.ecotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Transactional
    public void processActivity(User user , Activity activity ){
        List<UserChallenge> activeQuests = userChallengeRepository.findActiveChallenges(user.getEmail());

        for(UserChallenge quest : activeQuests){
            Challenge template = quest.getChallenge();

            boolean isRelevant = false;
            if ("ACTIVITY_COUNT".equals(template.getType())) {

                if (template.getCategory()==null || template.getCategory().equals(activity.getActivityCategory())) {
                    isRelevant = true;
                    quest.setCurrentValue(quest.getCurrentValue().add(BigDecimal.ONE));
                }
            }

            if(isRelevant && quest.getCurrentValue().compareTo(template.getTargetValue()) >=0){
                quest.setStatus("COMPLETED");
                quest.setCompletedAt(LocalDateTime.now());
            }

            userChallengeRepository.save(quest);
            simpMessagingTemplate.convertAndSend("/topic/updates/" + user.getEmail(), "REFETCH_CHALLENGES");

        }
    }


    public void acceptChallenge(Map<String,Object> data , User user) {
        String challengeCode = (String) data.get("code");

        Challenge template = challengeRepository.findByCode(challengeCode)
                .orElseGet(() -> {
                    Challenge newTemplate = new Challenge();
                    newTemplate.setCode(challengeCode);
                    newTemplate.setTitle((String) data.get("title"));
                    newTemplate.setType((String) data.get("type"));
                    newTemplate.setTargetValue(new BigDecimal(data.get("target").toString()));
                    newTemplate.setCategory((String) data.get("cat"));
                    newTemplate.setDifficulty("medium");
                    return challengeRepository.save(newTemplate);
                });
        if (!userChallengeRepository.existsByUserEmailAndChallengeCodeAndStatus(user.getEmail(), challengeCode, "ACTIVE")) {
            UserChallenge uc = new UserChallenge();
            uc.setUser(user);
            uc.setChallenge(template);
            uc.setStatus("PROPOSED"); // <--- Set to PROPOSED so user can accept/reject in UI
            userChallengeRepository.save(uc);

        }
    }

    @Transactional
    public void startChallenge(String code , String email) {
        UserChallenge uc = userChallengeRepository.findProposedChallenge(email,code).orElseThrow();
        uc.setStatus("ACTIVE");
        uc.setStartedAt(LocalDateTime.now());
        userChallengeRepository.save(uc);
        simpMessagingTemplate.convertAndSend("/topic/updates/" + email, "REFETCH_CHALLENGES");
    }
}
