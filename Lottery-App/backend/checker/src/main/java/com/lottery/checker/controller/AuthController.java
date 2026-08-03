package com.lottery.checker.controller;

import com.lottery.checker.dto.request.LoginRequest;
import com.lottery.checker.dto.request.RegisterRequest;
import com.lottery.checker.dto.request.SocialLoginRequest;
import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.AuthResponse;
import com.lottery.checker.dto.response.PasswordRulesResponse;
import com.lottery.checker.service.AuthService;
import com.lottery.checker.service.SocialAuthService;
import com.lottery.checker.service.UserService;
import com.lottery.checker.validation.PasswordRulesHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final SocialAuthService socialAuthService;
    private final AuthService authService;
    private final PasswordRulesHolder passwordRulesHolder;

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
                passwordRulesHolder.getMinLength(),
                passwordRulesHolder.getMaxLength(),
                passwordRulesHolder.getBlocklist()
        );

        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }
}