package com.lottery.checker.dto.response;

import com.lottery.checker.entity.Role;
import com.lottery.checker.entity.User;
import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String userCode,
    String email,
    String phone,
    String fullName,
    Role role,
    Boolean isActive,
    LocalDateTime lastLogin,
    LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        LocalDateTime lastLogin = user.getLastLogin() != null
            ? user.getLastLogin()
            : user.getCreatedAt();
        return new UserResponse(
            user.getId(),
            user.getUserCode(),
            user.getEmail(),
            user.getPhone(),
            user.getFullName(),
            user.getRole(),
            user.getIsActive(),
            lastLogin,
            user.getCreatedAt()
        );
    }
}