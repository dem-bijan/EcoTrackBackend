package com.ecotrack.dto;


import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    private List<ChatMessage> messages;

    @Data
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
