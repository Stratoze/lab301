package com.lottery.checker.service;

import com.lottery.checker.dto.request.LoginRequest;
import com.lottery.checker.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);
}