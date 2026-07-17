package com.lottery.checker.controller;

import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.LinkedAccountsResponse;
import com.lottery.checker.dto.response.UserResponse;
import com.lottery.checker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMe(principal.getName())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(Principal principal, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateMe(principal.getName(), body)));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(Principal principal, @RequestBody Map<String, String> body) {
        userService.changePassword(principal.getName(), body.get("oldPassword"), body.get("newPassword"));
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @GetMapping("/linked-accounts")
    public ResponseEntity<ApiResponse<LinkedAccountsResponse>> getLinkedAccounts(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getLinkedAccounts(principal.getName())));
    }

    @PostMapping("/link-social")
    public ResponseEntity<ApiResponse<String>> linkSocialAccount(Principal principal, @RequestBody Map<String, String> body) {
        userService.linkSocialAccount(principal.getName(), body.get("provider"), body.get("token"));
        return ResponseEntity.ok(ApiResponse.success("Account linked successfully"));
    }

    @PostMapping("/unlink-phone")
    public ResponseEntity<ApiResponse<String>> unlinkPhone(Principal principal) {
        userService.unlinkPhone(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Phone number unlinked"));
    }
}