package com.lottery.checker.controller;

import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
public class PasswordController {

    private final UserService userService;

    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody com.lottery.checker.dto.request.ForgotPasswordRequest request) {
        userService.requestPasswordReset(request.email());
        return ResponseEntity.ok(ApiResponse.success("If the email exists, a reset link has been sent."));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody com.lottery.checker.dto.request.ResetPasswordRequest request) {
        userService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully."));
    }
    
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> validateResetToken(
            @RequestParam("token") String resetCode
    ) {
        boolean valid = userService.isResetTokenValid(resetCode);

        return ResponseEntity.ok(
                ApiResponse.success(Map.of("valid", valid))
        );
    }
}