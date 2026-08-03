package com.lottery.checker.service;

import com.lottery.checker.dto.request.RegisterRequest;
import com.lottery.checker.dto.response.LinkedAccountsResponse;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.UserResponse;
import com.lottery.checker.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserResponse> getUsersByIds(List<Long> ids);

    User register(RegisterRequest request);

    User findByEmail(String email);

    Optional<User> findByEmailOptional(String email);

    PagedResponse<UserResponse> getAllUsers(String keyword, Pageable pageable);

    PagedResponse<UserResponse> getAllUsers(String keyword, String loginFilter, Pageable pageable);

    void updateStatus(List<Long> ids, boolean isActive);

    UserResponse updateUser(Long id, com.lottery.checker.dto.request.UpdateUserRequest request);

    UserResponse getMe(String email);

    UserResponse updateMe(String email, com.lottery.checker.dto.request.UpdateMeRequest request);

    void changePassword(String email, com.lottery.checker.dto.request.ChangePasswordRequest request);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    boolean isResetTokenValid(String resetCode);

    void sendBulkEmail(List<Long> ids, String subject, String content);

    void updateLastLogin(String email);

    LinkedAccountsResponse getLinkedAccounts(String email);

    void linkSocialAccount(String email, com.lottery.checker.dto.request.LinkSocialAccountRequest request);

    void unlinkPhone(String email);
}