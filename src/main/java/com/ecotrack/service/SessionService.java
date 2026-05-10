package com.ecotrack.service;

import com.ecotrack.entity.UserSession;
import com.ecotrack.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final UserSessionRepository userSessionRepository;

    /** Register a new login session for a user */
    public void registerNewSession(String email, String issuedAt) {
        UserSession session = new UserSession();
        session.setEmail(email);
        session.setLastIssuedAt(issuedAt);
        session.setLoginTime(LocalDateTime.now());
        userSessionRepository.save(session);
    }

    /** Validate that the JWT's issuedAt matches the stored session */
    public boolean isTokenValidForSession(String email, String tokenIssuedAt) {
        return userSessionRepository.findById(email)
                .map(session -> session.getLastIssuedAt().equals(tokenIssuedAt))
                .orElse(true); // If no session record, allow (first login)
    }

    public void addChatMessage(String email ,String role ,String content ,String impactSummary) {

        UserSession session = userSessionRepository.findById(email).orElseGet(()->{
            UserSession session1 = new UserSession();
            session1.setEmail(email);
            session1.setLoginTime(LocalDateTime.now());
            session1.setLastIssuedAt(String.valueOf(System.currentTimeMillis()));
            return session1;
        });

        if (session.getChatHistory()==null){
            session.setChatHistory(new ArrayList<>());
        };

        UserSession.ChatMessage msg = new UserSession.ChatMessage();
        msg.setRole(role);
        msg.setContent(content);
        msg.setImpactSummary(impactSummary);
        msg.setTimestamp(LocalDateTime.now());

        session.getChatHistory().add(msg);
        session.getChatHistory().removeIf(m ->
                m.getTimestamp().isBefore(LocalDateTime.now().minusHours(24)));

        userSessionRepository.save(session);
    }

    public List<UserSession.ChatMessage> getChatHistory(String email) {
        return userSessionRepository.findById(email).map(session -> {if (session.getChatHistory() == null) return new ArrayList<UserSession.ChatMessage>();
        boolean removed = session.getChatHistory().removeIf(m ->
                m.getTimestamp().isBefore(LocalDateTime.now().minusHours(24)));

        if (removed) {
            userSessionRepository.save(session);
        }
        return session.getChatHistory();

    }).orElse(new ArrayList<>());

    }

}

