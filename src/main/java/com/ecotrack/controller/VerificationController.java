package com.ecotrack.controller;


import com.ecotrack.entity.User;
import com.ecotrack.repository.UserRepository;
import com.ecotrack.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verify")
@RequiredArgsConstructor
public class VerificationController {

    private final UserRepository userRepository ;
    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<?> sendCode() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        String code = String.valueOf((int)(Math.random()*900000)+100000);
        user.setVerificationCode(code);
        userRepository.save(user);

        emailService.sendVerificationMail(email,code);
        return ResponseEntity.ok("Code Sent successfully");
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmCode(@RequestBody java.util.Map<String,String> body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        if (user.getVerificationCode().equals(body.get("code"))){
            user.setEmailVerified(true);
            user.setVerificationCode(null);
            userRepository.save(user);
            return ResponseEntity.ok("Email verified Successfully");
        }
        return ResponseEntity.status(400).body("Invalid verification code");
    }

}
