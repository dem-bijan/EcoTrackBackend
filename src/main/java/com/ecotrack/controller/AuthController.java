package com.ecotrack.controller;
import com.ecotrack.dto.AuthResponse;
import com.ecotrack.dto.LoginRequest;
import com.ecotrack.dto.RegisterRequest;
import com.ecotrack.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public  ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String responseMessage = authService.register(request);
        return ResponseEntity.ok(responseMessage);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse>
    login(@RequestBody LoginRequest request) {
        AuthResponse response =
                authService.login(request);
        return ResponseEntity.ok(response);
    }

}

