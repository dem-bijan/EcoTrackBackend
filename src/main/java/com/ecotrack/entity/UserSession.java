package com.ecotrack.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "user_sessions")
public class UserSession {
    @Id
    private String email;
    private String lastIssuedAt;
    private LocalDateTime loginTime;

    private List<ChatMessage> chatHistory = new ArrayList<>();

    @Data
    public static class  ChatMessage {
        private String role;
        private String content;
        private String impactSummary;
        private LocalDateTime timestamp;


    }
}
