package com.lottery.checker.service;

import com.lottery.checker.dto.request.RegisterRequest;
import com.lottery.checker.dto.response.LinkedAccountsResponse;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.UserResponse;
import com.lottery.checker.entity.Role;
import com.lottery.checker.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface UserService {
    List<UserResponse> getUsersByIds(List<Long> ids);
    User register(RegisterRequest request);
    User findByEmail(String email);
    PagedResponse<UserResponse> getAllUsers(String keyword, Pageable pageable);
    void updateStatus(List<Long> ids, boolean isActive);
    UserResponse updateUser(Long id, String fullName, String phone, Role role, Boolean isActive);
    UserResponse getMe(String email);
    UserResponse updateMe(String email, Map<String, String> updates);
    void changePassword(String email, String oldPassword, String newPassword);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    void sendBulkEmail(List<Long> ids, String subject, String content);
    void updateLastLogin(String email);
    LinkedAccountsResponse getLinkedAccounts(String email);
    void linkSocialAccount(String email, String provider, String token);
    void unlinkPhone(String email);
}