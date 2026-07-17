package com.lottery.checker.service;

import com.lottery.checker.dto.request.SocialLoginRequest;
import com.lottery.checker.dto.response.AuthResponse;
import com.lottery.checker.entity.User;

public interface SocialAuthService {
    AuthResponse authenticate(SocialLoginRequest request);
    void linkProvider(User user, String provider, String token);
}