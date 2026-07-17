package com.lottery.checker.dto.response;

import com.lottery.checker.entity.Role;

public record AuthResponse(
    String token,
    String userCode,
    String fullName,
    Role role
) {}