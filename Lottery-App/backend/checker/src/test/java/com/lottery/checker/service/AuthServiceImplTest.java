package com.lottery.checker.service;

import com.lottery.checker.dto.request.LoginRequest;
import com.lottery.checker.dto.response.AuthResponse;
import com.lottery.checker.entity.Role;
import com.lottery.checker.entity.User;
import com.lottery.checker.exception.ForbiddenException;
import com.lottery.checker.exception.UnauthorizedException;
import com.lottery.checker.security.JwtService;
import com.lottery.checker.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;
    private User blockedUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(3L)
                .userCode("USR-10-2023-00000003")
                .email("khach1@gmail.com")
                .password("$2a$12$hashedpassword...")
                .fullName("Le Van Tam")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        blockedUser = User.builder()
                .id(9L)
                .userCode("USR-11-2023-00000001")
                .email("locked@gmail.com")
                .password("$2a$12$hashedblocked...")
                .fullName("Nguoi Bi Khoa")
                .role(Role.ROLE_USER)
                .isActive(false)
                .build();
    }

    @Test
    void login_ValidCredentials_ReturnsAuthResponseWithToken() {
        LoginRequest request = new LoginRequest("khach1@gmail.com", "correctPassword");

        when(userService.findByEmailOptional("khach1@gmail.com"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("correctPassword", activeUser.getPassword()))
                .thenReturn(true);
        when(jwtService.generateToken(eq("khach1@gmail.com"), anyMap()))
                .thenReturn("mock.jwt.token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("mock.jwt.token");
        assertThat(response.userCode()).isEqualTo("USR-10-2023-00000003");
        assertThat(response.fullName()).isEqualTo("Le Van Tam");
        assertThat(response.role()).isEqualTo(Role.ROLE_USER);
        verify(userService).updateLastLogin("khach1@gmail.com");
    }

    @Test
    void login_UnknownEmail_ThrowsUnauthorized() {
        LoginRequest request = new LoginRequest("unknown@gmail.com", "correctPassword");

        when(userService.findByEmailOptional("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password.");
    }

    @Test
    void login_BlockedUser_ThrowsForbidden() {
        LoginRequest request = new LoginRequest("locked@gmail.com", "correctPassword");

        when(userService.findByEmailOptional("locked@gmail.com"))
                .thenReturn(Optional.of(blockedUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Your account is blocked.");
    }

    @Test
    void login_WrongPassword_ThrowsUnauthorized() {
        LoginRequest request = new LoginRequest("khach1@gmail.com", "wrongPassword");

        when(userService.findByEmailOptional("khach1@gmail.com"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongPassword", activeUser.getPassword()))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password.");
    }

    @Test
    void login_SocialAccountWithoutPassword_ThrowsUnauthorized() {
        User socialOnlyUser = User.builder()
                .id(5L)
                .userCode("USR-10-2023-00000005")
                .email("social@gmail.com")
                .password(null)
                .fullName("Social User")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        LoginRequest request = new LoginRequest("social@gmail.com", "anyPassword");

        when(userService.findByEmailOptional("social@gmail.com"))
                .thenReturn(Optional.of(socialOnlyUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password.");
    }

    @Test
    @SuppressWarnings("unchecked")
    void login_ValidCredentials_TokenContainsRoleUserCodeAndFullNameClaims() {
        LoginRequest request = new LoginRequest("khach1@gmail.com", "correctPassword");

        when(userService.findByEmailOptional("khach1@gmail.com"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("correctPassword", activeUser.getPassword()))
                .thenReturn(true);

        ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
        when(jwtService.generateToken(eq("khach1@gmail.com"), claimsCaptor.capture()))
                .thenReturn("mock.jwt.token");

        authService.login(request);

        Map<String, Object> claims = claimsCaptor.getValue();
        assertThat(claims).containsEntry("role", "ROLE_USER");
        assertThat(claims).containsEntry("userCode", "USR-10-2023-00000003");
        assertThat(claims).containsEntry("fullName", "Le Van Tam");
    }
}