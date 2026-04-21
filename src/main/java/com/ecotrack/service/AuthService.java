package com.ecotrack.service;

import com.ecotrack.dto.AuthResponse;
import com.ecotrack.dto.RegisterRequest;
import com.ecotrack.dto.LoginRequest;
import com.ecotrack.entity.User;
import com.ecotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;


    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        // GENERATE THE JWT TOKEN AND PASS IT TO NEXT.JS!
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token);
    }

        public String register(RegisterRequest request) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already Taken!");
            }

            User user = new User();
            user.setFullName(request.getFullname());
            user.setEmail(request.getEmail());
            user.setPasswordHash(passwordEncoder.encode(request.getPasswordhash()));
            userRepository.save(user);

            return "User Registered successfully";

        }

}
