package com.ecotrack.service;


import com.ecotrack.dto.UpdateEmailRequest;
import com.ecotrack.dto.UpdateNameRequest;
import com.ecotrack.dto.UpdatePasswordRequest;
import com.ecotrack.repository.UserRepository;
import com.ecotrack.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserModifService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public void updatePassword(String email , UpdatePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow();

        if(!passwordEncoder.matches(request.getCurrentPassword(),user.getPasswordHash())) {
            throw new RuntimeException("Current password incorrect");
        };
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void updateEmail(String oldEmail , UpdateEmailRequest request) {
        User user = userRepository.findByEmail(oldEmail).orElseThrow();

        if (userRepository.findByEmail(request.getNewEmail()).isPresent()) {
            throw new RuntimeException("Email already taken");
        }
        user.setEmail(request.getNewEmail());
        user.setEmailVerified(false);
        userRepository.save(user);

    }

    public void updateName(String email , UpdateNameRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setFullName(request.getNewName());
        userRepository.save(user);
    }

}

