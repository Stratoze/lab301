package com.lottery.checker.controller;

import com.lottery.checker.dto.request.ChangePasswordRequest;
import com.lottery.checker.dto.request.LinkSocialAccountRequest;
import com.lottery.checker.dto.request.UpdateMeRequest;
import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.LinkedAccountsResponse;
import com.lottery.checker.dto.response.UserResponse;
import com.lottery.checker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;

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
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(Principal principal, @Valid @RequestBody UpdateMeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateMe(principal.getName(), request)));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(Principal principal, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @GetMapping("/linked-accounts")
    public ResponseEntity<ApiResponse<LinkedAccountsResponse>> getLinkedAccounts(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getLinkedAccounts(principal.getName())));
    }

    @PostMapping("/link-social")
    public ResponseEntity<ApiResponse<String>> linkSocialAccount(Principal principal, @Valid @RequestBody LinkSocialAccountRequest request) {
        userService.linkSocialAccount(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Account linked successfully"));
    }

    @PostMapping("/unlink-phone")
    public ResponseEntity<ApiResponse<String>> unlinkPhone(Principal principal) {
        userService.unlinkPhone(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Phone number unlinked"));
    }
}