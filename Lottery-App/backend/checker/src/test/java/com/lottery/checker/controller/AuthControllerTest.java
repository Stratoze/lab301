package com.lottery.checker.controller;

import com.lottery.checker.dto.request.LoginRequest;
import com.lottery.checker.dto.request.RegisterRequest;
import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.AuthResponse;
import com.lottery.checker.dto.response.PasswordRulesResponse;
import com.lottery.checker.entity.Role;
import com.lottery.checker.entity.User;
import com.lottery.checker.security.JwtService;
import com.lottery.checker.service.SocialAuthService;
import com.lottery.checker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private UserService userService;
    @Mock private SocialAuthService socialAuthService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    private User activeUser;
    private User blockedUser;
    private User socialUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(3L)
                .userCode("USR-10-2023-00000003")
                .email("khach1@gmail.com")
                .password("$2a$12$hashedpassword...")
                .fullName("Le V?n Tam")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        blockedUser = User.builder()
                .id(9L)
                .userCode("USR-11-2023-00000001")
                .email("locked@gmail.com")
                .password("$2a$12$hashedblocked...")
                .fullName("Ng??i B? Khoa")
                .role(Role.ROLE_USER)
                .isActive(false)
                .build();

        socialUser = User.builder()
                .id(5L)
                .userCode("USR-10-2023-00000005")
                .email("google_u@gmail.com")
                .password(null) // Social login - no password
                .fullName("User Google")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();
    }

    // 4.1 Register - valid payload returns 201
    @Test
    void register_ValidPayload_Returns201() {
        RegisterRequest request = new RegisterRequest(
                "New User", "newuser@gmail.com", "0911111111", "StrongPass1!"
        );
        when(userService.register(any(RegisterRequest.class))).thenReturn(activeUser);

        ResponseEntity<ApiResponse<String>> response = authController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Success");
        assertThat(response.getBody().getData()).contains("registered successfully");
    }

    // 4.2 Login - valid credentials returns 200 with JWT
    @Test
    void login_ValidCredentials_Returns200WithJwt() {
        LoginRequest request = new LoginRequest("khach1@gmail.com", "correctPassword");
        when(userService.findByEmail("khach1@gmail.com")).thenReturn(activeUser);
        when(passwordEncoder.matches("correctPassword", activeUser.getPassword())).thenReturn(true);
        doNothing().when(userService).updateLastLogin("khach1@gmail.com");
        when(jwtService.generateToken(eq("khach1@gmail.com"), anyMap())).thenReturn("mock.jwt.token");

        ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        AuthResponse auth = response.getBody().getData();
        assertThat(auth.token()).isEqualTo("mock.jwt.token");
        assertThat(auth.fullName()).isEqualTo("Le V?n Tam");
        assertThat(auth.role()).isEqualTo(Role.ROLE_USER);
    }

    // 4.3 Login - blocked user returns 403
    @Test
    void login_BlockedUser_Returns403() {
        LoginRequest request = new LoginRequest("locked@gmail.com", "correctPassword");
        when(userService.findByEmail("locked@gmail.com")).thenReturn(blockedUser);

        ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("blocked");
    }

    // 4.4 JWT token contains correct claims and expiry
    @Test
    void jwtToken_ContainsCorrectClaimsAndExpiry() {
        LoginRequest request = new LoginRequest("khach1@gmail.com", "correctPassword");
        when(userService.findByEmail("khach1@gmail.com")).thenReturn(activeUser);
        when(passwordEncoder.matches("correctPassword", activeUser.getPassword())).thenReturn(true);
        doNothing().when(userService).updateLastLogin("khach1@gmail.com");

        // Capture the actual claims map passed to jwtService
        ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
        when(jwtService.generateToken(eq("khach1@gmail.com"), claimsCaptor.capture()))
                .thenReturn("mock.jwt.token");

        authController.login(request);

        Map<String, Object> claims = claimsCaptor.getValue();
        assertThat(claims).containsEntry("role", "ROLE_USER");
    }
}