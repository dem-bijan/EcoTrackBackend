package com.ecotrack.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai/public")
@CrossOrigin(origins = "http://localhost:3000")
public class AiPublicController {
    private final ChatClient chatClient;

    public AiPublicController(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> answer(@RequestBody Map<String, String> request) {
        String message = request.get("message");

        String aiResponse = chatClient.prompt()
                .user(message)
                .call()
                .content();

        Map<String, String> response = new HashMap<>();
        response.put("response", aiResponse);

        return ResponseEntity.ok(response);
    }
}