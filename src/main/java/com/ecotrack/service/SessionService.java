package com.ecotrack.service;

import com.ecotrack.entity.UserSession;
import com.ecotrack.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

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
}
