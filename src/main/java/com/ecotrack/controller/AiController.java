package com.ecotrack.controller;

import com.ecotrack.dto.ChatRequest;
import com.ecotrack.entity.Activity;
import com.ecotrack.entity.Recommendation;
import com.ecotrack.service.ActivityService;
import com.ecotrack.service.RecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai") // Changed from /api/ollama to /api/ai
@CrossOrigin(origins = "http://localhost:3001")
public class AiController {
    private final ChatClient chatClient;
    private final ActivityService activityService;
    private final RecommendationService recommendationService;
    private final ObjectMapper objectMapper;

    // Spring will automatically inject the OpenAiChatModel because of the starter
    public AiController(ChatModel chatModel, ActivityService activityService, RecommendationService recommendationService, ObjectMapper objectMapper) {
        this.activityService = activityService;
        this.recommendationService = recommendationService;
        this.objectMapper = objectMapper;
        this.chatClient = ChatClient.create(chatModel);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> getAnswer(@RequestBody ChatRequest chatRequest) {
        String systemPrompt = """
                 # EcoTrack AI - Minimalist Auditor & Analyst
                ## ROLE
                You are a concise sustainability assistant.
            
                ## RULES
                1. BREVITY: Keep chat responses under 15 words.
                2. DATA GATHERING: Ask one question if activity details are missing.
                3. LOGGING: Append [LOG:{"type":"...","cat":"...","desc":"...","impact":0.0,"summary":"...","data":{}}]
                4. PATTERN ANALYSIS: Analyze the history of this conversation. If you detect a recurring habit\s
                   or a major area for improvement, append a recommendation tag:
                   [REC:{"title":"...","desc":"...","type":"transport|energy|food","impact":0.0,"difficulty":"easy|medium|hard"}]
                   (Only use [REC] if the pattern is clear).
            """;


        List<Message> history = chatRequest.getMessages().stream()
                .map(m -> m.getRole().equals("user")
                        ? new UserMessage(m.getContent())
                        : new AssistantMessage(m.getContent()))
                .collect(Collectors.toList());

        String aiResponse = chatClient.prompt()
                .system(systemPrompt)
                .messages(history)
                .call()
                .content();

        Map<String, String> result = new HashMap<>();
        String extractedSummary = null;

        if (aiResponse.contains("[LOG:")) {
            try {
                int start = aiResponse.indexOf("[LOG:") + 5;
                int end = aiResponse.lastIndexOf("]");
                String jsonPart = aiResponse.substring(start, end);
                
                Map<String, Object> logMap = objectMapper.readValue(jsonPart, Map.class);
                extractedSummary = (String) logMap.get("summary");
                
                Activity activity = new Activity();
                activity.setActivityType((String) logMap.get("type"));
                activity.setActivityCategory((String) logMap.get("cat"));
                activity.setDescription((String) logMap.get("desc"));
                activity.setCo2Impact(new BigDecimal(logMap.get("impact").toString()));
                activity.setActivityData((Map<String, Object>) logMap.get("data"));
                activity.setActivityDate(LocalDate.now());

                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                activityService.logActivity(email, activity);

                aiResponse = aiResponse.substring(0, aiResponse.indexOf("[LOG:")).trim();
            } catch (Exception e) {
                System.err.println("Failed to log activity: " + e.getMessage());
            }
        }

        if(aiResponse.contains("[REC:")) {
            try {
                int start = aiResponse.indexOf("[REC:") + 5;
                int end = aiResponse.lastIndexOf("]");
                String jsonPart = aiResponse.substring(start, end);

                Map<String, Object> recMap = objectMapper.readValue(jsonPart, Map.class);
                Recommendation rec = new Recommendation();
                rec.setTitle((String) recMap.get("title"));
                rec.setDescription((String) recMap.get("description"));
                rec.setActionType((String) recMap.get("action_type"));
                rec.setEstimatedImpact(new BigDecimal(recMap.get("impact").toString()));
                rec.setDifficultyLevel((String) recMap.get("diff"));
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                recommendationService.saveRecomendation(email,rec);

                aiResponse = aiResponse.substring(0, aiResponse.indexOf("[LOG:")).trim();

            }
            catch (Exception e) {
                System.err.println("Failed to log recommendation: " + e.getMessage());
            }
        }

        result.put("text", aiResponse);
        result.put("summary", extractedSummary);

        return ResponseEntity.ok(result);
    }
}
