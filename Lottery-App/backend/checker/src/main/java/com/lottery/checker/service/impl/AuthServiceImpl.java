package com.lottery.checker.service.impl;

import com.lottery.checker.dto.request.LoginRequest;
import com.lottery.checker.dto.response.AuthResponse;
import com.lottery.checker.entity.User;
import com.lottery.checker.exception.ForbiddenException;
import com.lottery.checker.exception.UnauthorizedException;
import com.lottery.checker.security.JwtService;
import com.lottery.checker.service.AuthService;
import com.lottery.checker.service.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userService.findByEmailOptional(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!user.getIsActive()) {
            throw new ForbiddenException("Your account is blocked.");
        }

        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        userService.updateLastLogin(user.getEmail());

        String token = jwtService.generateToken(
                user.getEmail(),
                Map.of(
                        "role", user.getRole().name(),
                        "userCode", user.getUserCode(),
                        "fullName", user.getFullName()
                )
        );

        return new AuthResponse(
                token,
                user.getUserCode(),
                user.getFullName(),
                user.getRole()
        );
    }
}