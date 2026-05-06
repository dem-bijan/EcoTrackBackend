package com.ecotrack.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "user_sessions")
public class UserSession {
    @Id
    private String email;
    private String lastIssuedAt;
    private LocalDateTime loginTime;
}
