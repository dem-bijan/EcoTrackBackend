package com.ecotrack.controller;

import com.ecotrack.dto.ChatRequest;
import com.ecotrack.entity.*;
import com.ecotrack.repository.*;
import com.ecotrack.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:3000")
public class AiController {

    private final ChatClient chatClient;
    private final AchievementService achievementService;
    private final ActivityService activityService;
    private final ChallengeService challengeService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final SessionService sessionService;
    private final RecommendationService recommendationService;
    private final CarbonFootprintRepository footprintRepository;
    private final ObjectMapper objectMapper;

    public AiController(ChatModel chatModel, 
                        AchievementService achievementService, 
                        ActivityService activityService,
                        ChallengeService challengeService,
                        SimpMessagingTemplate simpMessagingTemplate, 
                        UserProfileRepository userProfileRepository, 
                        UserRepository userRepository, 
                        ActivityRepository activityRepository,
                        UserChallengeRepository userChallengeRepository,
                        SessionService sessionService,
                        RecommendationService recommendationService, 
                        CarbonFootprintRepository footprintRepository,
                        ObjectMapper objectMapper) {
        this.achievementService = achievementService;
        this.activityService = activityService;
        this.challengeService = challengeService;
        this.messagingTemplate = simpMessagingTemplate;
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.sessionService = sessionService;
        this.recommendationService = recommendationService;
        this.footprintRepository = footprintRepository;
        this.objectMapper = objectMapper;
        this.chatClient = ChatClient.create(chatModel);
    }

    @GetMapping("/history")
    public ResponseEntity<List<UserSession.ChatMessage>> getHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(sessionService.getChatHistory(email));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> getAnswer(@RequestBody ChatRequest chatRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        // 1. Build context-aware prompt
        String dynamicPrompt = buildContextAwarePrompt(user, email);

        // 2. Setup message history
        List<Message> messages = new ArrayList<>();
        messages.add(new org.springframework.ai.chat.messages.SystemMessage(dynamicPrompt));
        
        messages.addAll(chatRequest.getMessages().stream()
                .map(m -> m.getRole().equals("user") ? new UserMessage(m.getContent()) : new AssistantMessage(m.getContent()))
                .collect(Collectors.toList()));

        // 3. Persist user message
        String latestUserText = chatRequest.getMessages().get(chatRequest.getMessages().size() - 1).getContent();
        sessionService.addChatMessage(email, "user", latestUserText, null);

        // 4. Get AI response
        String aiResponse = chatClient.prompt().messages(messages).call().content();

        // 5. Parse tags and execute actions
        Map<String, String> result = parseAndExecuteTags(aiResponse, email, user);

        // 6. Persist AI response
        sessionService.addChatMessage(email, "ai", result.get("text"), result.get("summary"));

        return ResponseEntity.ok(result);
    }

    private String buildContextAwarePrompt(User user, String email) {
        UserProfile profile = userProfileRepository.findByUserEmail(email).orElse(null);
        
        // Pattern Recognition: Last 10 activities
        List<Activity> recentActs = activityRepository.findByUserIdOrderByActivityDateDesc(user.getId())
                .stream().limit(10).collect(Collectors.toList());
        
        String behaviorSummary = recentActs.stream()
                .map(a -> String.format("- %s (%s, %skg)", a.getDescription(), a.getActivityCategory(), a.getCo2Impact()))
                .collect(Collectors.joining("\n"));

        // Current Challenges awareness
        List<UserChallenge> activeQuests = userChallengeRepository.findActiveChallenges(email);
        String activeList = activeQuests.stream().map(q -> q.getChallenge().getTitle()).collect(Collectors.joining(", "));

        String currentDate = LocalDate.now().toString();

        return String.format("""
                # EcoTrack AI Agent - System Prompt
                Current Date: %s
                Current User: %s

                ## USER CONTEXT
                Lifestyle: Diet=%s, Vehicle=%s, Housing=%s
                Recent Behavior Patterns:
                %s
                
                Current Active Challenges: [%s]

                ## YOUR ROLE
                You are the EcoTrack Carbon Tracking AI. You extract carbon footprint data from user conversations and emit structured tags that the backend processes into the PostgreSQL database.

                ## DATABASE SCHEMA AWARENESS
                I populate these tables through your tags:
                1. **activities** - via [LOG:] tag
                2. **recommendations** - via [REC:] tag
                3. **carbon_footprints** - via [FP:] tag

                ## TAG EMISSION RULES

                ### 1. ACTIVITY LOGGING - [LOG:] Tag
                Emits when user describes an action with carbon impact.

                **Format:**
                [LOG:{"type":"activity_type","cat":"category","desc":"description","impact":kg_co2,"data":{"details":"..."},"summary":"one_line_summary"}]

                **Required Fields (maps to activities table):**
                - `type` -> activity_type (VARCHAR 50): Specific action identifier
                - `cat` -> activity_category (VARCHAR 50): Must be one of: "transportation", "food", "housing", "shopping", "waste"
                - `desc` -> description (TEXT): Human-readable explanation
                - `impact` -> co2_impact (DECIMAL 10,3): CO2 impact in KILOGRAMS (NOT TONS)
                  - POSITIVE for emissions
                  - NEGATIVE for savings
                - `data` -> activity_data (JSONB): Activity-specific details
                - `summary` -> Concise sentence for UI

                ### 2. RECOMMENDATION GENERATION - [REC:] Tag
                **Format:**
                [REC:{"title":"action_title","desc":"explanation","type":"action_category","impact":annual_tons_saved,"difficulty":"level"}]

                **Required Fields:**
                - `title` -> title (VARCHAR 255)
                - `desc` -> description (TEXT)
                - `type` -> action_type (Must match activity categories)
                - `impact` -> estimated_impact (Annual TONS saved)
                - `difficulty` -> difficulty_level ("easy", "medium", "hard")

                ### 3. FOOTPRINT SNAPSHOT - [FP:] Tag
                **Format:**
                [FP:{"tons":total_co2_tons,"breakdown":{"transportation":X,"food":Y,"housing":Z,"shopping":W,"waste":V}}]

                **Required Fields:**
                - `tons` -> total_co2_tons (TONS)
                - `breakdown` -> category totals in TONS. MUST be a valid nested JSON object:
                  {"transportation":0.25,"food":0.15,"housing":0.08,"shopping":0.02,"waste":0.00}
                  
                You are an active coach. Do not wait for the user to ask. If you detect a pattern (e.g.,
                too much driving, high food impact),you MUST immediately propose a challenge using the [CHALLENGE:]
                tag to help them improve that specific area

                ## CARBON IMPACT CALCULATION (CRITICAL FACTORS in kg)
                - Gasoline car: 0.411 kg/mile
                - Bus: 0.089 kg/mile | Train: 0.041 kg/mile
                - Domestic flight (economy): 0.255 kg/mile
                - Electricity (avg): 0.417 kg/kWh
                - Beef: 27.0 kg/kg | Chicken: 6.9 kg/kg
                - Meal savings (plant vs beef): -2.7 kg per meal
                - Recycling Aluminum: -9.0 kg/kg

                ## PERSONALIZATION & MEMORY RULES
                1. Use the [USER CONTEXT] to tailor your advice. 
                2. If the user mentions a lifestyle change, emit [UPDATE_PROFILE:{"field":"diet_type","value":"vegan"}].
                3. You can propose a custom challenge using [CHALLENGE:{"code":"...","title":"...","type":"ACTIVITY_COUNT","target":5,"cat":"transportation"}].

                ### 4. CONVERSATION FLOW & TRIGGERS
                1. LISTEN for activities.
                2. EXTRACT data (ask one clarifying question if distance/quantity missing).
                3. CALCULATE impact (Amount * Factor or search for methods).
                4. EMIT TAGS:
                   - MUST emit [LOG:] for any logged activity.
                   - MUST emit [REC:] for high-impact improvements.
                   - MUST emit [CHALLENGE:] for custom challenges that would help a user cut down his footprint
                   - **CRITICAL: You MUST emit [FP:] IMMEDIATELY if the user explicitly asks about their "score", 
                     "footprint", "total", or asks you to "update" or "calculate" it.** Do not just say you did it;
                      you must output the exact [FP:...] tag.
                5. RESPOND NATURALLY with encouragement.
                """, 
                currentDate, email, 
                profile != null ? profile.getDietType() : "Unknown",
                profile != null ? profile.getVehicleType() : "Unknown",
                profile != null ? profile.getHousingType() : "Unknown",
                behaviorSummary.isEmpty() ? "No history yet." : behaviorSummary,
                activeList.isEmpty() ? "None" : activeList
        );
    }

    private Map<String, String> parseAndExecuteTags(String aiResponse, String email, User user) {
        Map<String, String> result = new HashMap<>();
        String summary = null;

        if (aiResponse.contains("[LOG:")) summary = handleLogTag(aiResponse, email);
        if (aiResponse.contains("[REC:")) handleRecommendationTag(aiResponse, email);
        if (aiResponse.contains("[FP:")) handleFootprintTag(aiResponse, email, user);
        if (aiResponse.contains("[CHALLENGE:")) handleChallengeTag(aiResponse, email, user);
        if (aiResponse.contains("[UPDATE_PROFILE:")) handleProfileUpdateTag(aiResponse, email);

        String cleanedText = aiResponse
                .replaceAll("(?s)\\[LOG:.*?\\]", "")
                .replaceAll("(?s)\\[REC:.*?\\]", "")
                .replaceAll("(?s)\\[FP:.*?\\]", "")
                .replaceAll("(?s)\\[CHALLENGE:.*?\\]", "")
                .replaceAll("(?s)\\[UPDATE_PROFILE:.*?\\]", "")
                .trim();

        result.put("text", cleanedText);
        result.put("summary", summary);
        return result;
    }

    private String handleLogTag(String response, String email) {
        try {
            Map<String, Object> data = extractJson(response, "[LOG:");
            Activity activity = new Activity();
            activity.setActivityType((String) data.get("type"));
            activity.setActivityCategory((String) data.get("cat"));
            activity.setDescription((String) data.get("desc"));
            activity.setCo2Impact(new BigDecimal(data.get("impact").toString()));
            activity.setActivityData((Map<String, Object>) data.get("data"));
            activity.setActivityDate(LocalDate.now());

            activityService.logActivity(email, activity);
            achievementService.checkAchievements(email);
            challengeService.processActivity(userRepository.findByEmail(email).get(), activity); // Trigger Challenges!
            
            messagingTemplate.convertAndSend("/topic/updates/" + email, "REFETCH_ACTS");
            return (String) data.get("summary");
        } catch (Exception e) { return null; }
    }

    private void handleRecommendationTag(String response, String email) {
        try {
            Map<String, Object> data = extractJson(response, "[REC:");
            Recommendation rec = new Recommendation();
            rec.setTitle((String) data.get("title"));
            rec.setDescription((String) data.get("desc"));
            rec.setActionType((String) data.get("type"));
            rec.setEstimatedImpact(new BigDecimal(data.get("impact").toString()));
            rec.setDifficultyLevel((String) data.get("difficulty"));

            recommendationService.saveRecomendation(email, rec);
            messagingTemplate.convertAndSend("/topic/updates/" + email, "REFETCH_RECS");
        } catch (Exception e) {}
    }

    private void handleFootprintTag(String response, String email, User user) {
        try {
            Map<String, Object> data = extractJson(response, "[FP:");
            CarbonFootprint fp = new CarbonFootprint();
            fp.setUser(user);
            fp.setTotalCo2Tons(new BigDecimal(data.get("tons").toString()));
            fp.setBreakdown((Map<String, Object>) data.get("breakdown"));
            fp.setCalculationDate(LocalDate.now());
            fp.setIsBaseline(false);

            footprintRepository.save(fp);
            messagingTemplate.convertAndSend("/topic/updates/" + email, "REFETCH_TRENDS");
        } catch (Exception e) {}
    }

    private void handleChallengeTag(String response, String email, User user) {
        try {
            Map<String, Object> data = extractJson(response, "[CHALLENGE:");
            challengeService.acceptChallenge(data, user);
            messagingTemplate.convertAndSend("/topic/updates/" + email, "REFETCH_CHALLENGES");
        } catch (Exception e) {
            System.err.println("Failed to stage Challenge " + e.getMessage());
        }
    }

    private void handleProfileUpdateTag(String response, String email) {
        try {
            Map<String, Object> data = extractJson(response, "[UPDATE_PROFILE:");
            UserProfile profile = userProfileRepository.findByUserEmail(email).orElseThrow();
            String field = (String) data.get("field");
            String value = (String) data.get("value");

            if ("diet_type".equals(field)) profile.setDietType(value);
            if ("vehicle_type".equals(field)) profile.setVehicleType(value);
            if ("housing_type".equals(field)) profile.setHousingType(value);
            
            userProfileRepository.save(profile);
        } catch (Exception e) {}
    }

    private Map<String, Object> extractJson(String text, String tag) throws Exception {
        int start = text.indexOf(tag) + tag.length();
        int end = text.indexOf("]", start);
        String json = text.substring(start, end).trim();
        if (!json.startsWith("{")) json = "{" + json;
        if (!json.endsWith("}")) json = json + "}";
        return objectMapper.readValue(json, Map.class);
    }
}