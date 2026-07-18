package com.lottery.checker.controller;

import com.lottery.checker.dto.request.LoginRequest;
import com.lottery.checker.dto.request.RegisterRequest;
import com.lottery.checker.dto.request.SocialLoginRequest;
import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.AuthResponse;
import com.lottery.checker.dto.response.PasswordRulesResponse;
import com.lottery.checker.entity.User;
import com.lottery.checker.service.SocialAuthService;
import com.lottery.checker.service.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final SocialAuthService socialAuthService;
    private final PasswordEncoder passwordEncoder;
    private final com.lottery.checker.security.JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully"));
    }

    @PostMapping("/social")
    public ResponseEntity<ApiResponse<AuthResponse>> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        AuthResponse response = socialAuthService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/password-rules")
    public ResponseEntity<ApiResponse<PasswordRulesResponse>> getPasswordRules() {
        PasswordRulesResponse rules = new PasswordRulesResponse(
                com.lottery.checker.validation.PasswordRulesHolder.getMinLength(),
                com.lottery.checker.validation.PasswordRulesHolder.getMaxLength(),
                com.lottery.checker.validation.PasswordRulesHolder.getBlocklist()
        );
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByEmail(request.email());
        
        if (!user.getIsActive()) {
            return ResponseEntity.status(403).body(ApiResponse.error("Your account is blocked."));
        }

        // Verify password using BCrypt (social login accounts have null password)
        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid email or password."));
        }

        // Update last login timestamp
        userService.updateLastLogin(user.getEmail());

        String token = jwtService.generateToken(user.getEmail(), Map.of("role", user.getRole().name()));
        
        AuthResponse response = new AuthResponse(
                token,
                user.getUserCode(),
                user.getFullName(),
                user.getRole()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}