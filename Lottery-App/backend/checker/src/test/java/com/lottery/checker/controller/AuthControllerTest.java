package com.lottery.checker.controller;

import com.lottery.checker.dto.request.LoginRequest;
import com.lottery.checker.dto.request.RegisterRequest;
import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.AuthResponse;
import com.lottery.checker.entity.Role;
import com.lottery.checker.entity.User;
import com.lottery.checker.exception.ForbiddenException;
import com.lottery.checker.exception.UnauthorizedException;
import com.lottery.checker.service.AuthService;
import com.lottery.checker.service.SocialAuthService;
import com.lottery.checker.service.UserService;
import com.lottery.checker.validation.PasswordRulesHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private SocialAuthService socialAuthService;

    @Mock
    private AuthService authService;

    @Mock
    private PasswordRulesHolder passwordRulesHolder;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_ValidPayload_Returns201() {
        RegisterRequest request = new RegisterRequest(
                "New User",
                "newuser@gmail.com",
                "0911111111",
                "StrongPass1!"
        );

        when(userService.register(any(RegisterRequest.class)))
                .thenReturn(User.builder().id(3L).build());

        ResponseEntity<ApiResponse<String>> response = authController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Success");
        assertThat(response.getBody().getData()).contains("registered successfully");
    }

    @Test
    void login_ValidRequest_DelegatesToAuthService() {
        LoginRequest request = new LoginRequest("khach1@gmail.com", "correctPassword");
        AuthResponse authResponse = new AuthResponse(
                "mock.jwt.token", "USR-10-2023-00000003", "Le Van Tam", Role.ROLE_USER);

        when(authService.login(request)).thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(authResponse);
    }

    @Test
    void login_InvalidCredentials_LetsUnauthorizedPropagate() {
        LoginRequest request = new LoginRequest("unknown@gmail.com", "whatever");

        when(authService.login(request))
                .thenThrow(new UnauthorizedException("Invalid email or password."));

        assertThatThrownBy(() -> authController.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password.");
    }

    @Test
    void login_BlockedUser_LetsForbiddenPropagate() {
        LoginRequest request = new LoginRequest("locked@gmail.com", "whatever");

        when(authService.login(request))
                .thenThrow(new ForbiddenException("Your account is blocked."));

        assertThatThrownBy(() -> authController.login(request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Your account is blocked.");
    }
}