package com.lottery.checker.service;

import com.lottery.checker.dto.request.RegisterRequest;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.UserResponse;
import com.lottery.checker.entity.Role;
import com.lottery.checker.entity.User;
import com.lottery.checker.repository.PasswordResetTokenRepository;
import com.lottery.checker.repository.UserAuthProviderRepository;
import com.lottery.checker.repository.UserRepository;
import com.lottery.checker.service.impl.UserServiceImpl;
import com.lottery.checker.validation.PasswordRulesHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserAuthProviderRepository userAuthProviderRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private JavaMailSender mailSender;
    @Mock private SocialAuthService socialAuthService;

    @InjectMocks
    private UserServiceImpl userService;

    private MockedStatic<PasswordRulesHolder> passwordRulesMock;

    private User testUser;
    private User adminUser;
    private User blockedUser;

    @BeforeEach
    void setUp() {
        passwordRulesMock = mockStatic(PasswordRulesHolder.class);
        passwordRulesMock.when(PasswordRulesHolder::getMinLength).thenReturn(10);
        passwordRulesMock.when(PasswordRulesHolder::getMaxLength).thenReturn(64);
        passwordRulesMock.when(PasswordRulesHolder::getBlocklist).thenReturn(Set.of());
        passwordRulesMock.when(PasswordRulesHolder::getDictionaryWords).thenReturn(Set.of());

        testUser = User.builder()
                .id(3L)
                .userCode("USR-10-2023-00000003")
                .email("khach1@gmail.com")
                .phone("0910000001")
                .password("$2a$12$hashedpassword...")
                .fullName("Le V?n Tam")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        adminUser = User.builder()
                .id(1L)
                .userCode("USR-10-2023-00000001")
                .email("admin@veso.vn")
                .phone("0900000001")
                .password("$2a$12$hashedadmin...")
                .fullName("Phan ??ng Duy Phuc")
                .role(Role.ROLE_ADMIN)
                .isActive(true)
                .build();

        blockedUser = User.builder()
                .id(9L)
                .userCode("USR-11-2023-00000001")
                .email("locked@gmail.com")
                .phone("0910000009")
                .password("$2a$12$hashedblocked...")
                .fullName("Ng??i B? Khoa")
                .role(Role.ROLE_USER)
                .isActive(false)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (passwordRulesMock != null) {
            passwordRulesMock.close();
        }
    }

    // 3.1 Register - valid user returns created user with BCrypt
    @Test
    void register_ValidUser_ReturnsCreatedUser() {
        RegisterRequest request = new RegisterRequest(
                "New User", "newuser@gmail.com", "0911111111", "StrongPass1!"
        );
        when(userRepository.findByEmail("newuser@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("StrongPass1!")).thenReturn("$2a$12$encoded...");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(16L);
            return u;
        });

        User created = userService.register(request);

        assertThat(created.getEmail()).isEqualTo("newuser@gmail.com");
        assertThat(created.getPassword()).isEqualTo("$2a$12$encoded...");
        assertThat(created.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(created.getIsActive()).isTrue();
        assertThat(created.getUserCode()).startsWith("USR-");
        verify(passwordEncoder).encode("StrongPass1!");
    }

    // 3.2 Register - duplicate email throws conflict
    @Test
    void register_DuplicateEmailOrPhone_ThrowsConflict() {
        RegisterRequest request = new RegisterRequest(
                "Le V?n Tam", "khach1@gmail.com", "0910000999", "StrongPass1!"
        );
        when(userRepository.findByEmail("khach1@gmail.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
        verify(userRepository, never()).save(any());
    }

    // 3.3 Login - valid email returns correct user with active flag set
    @Test
    void findByEmail_ActiveUser_ReturnsUserWithCorrectFields() {
        when(userRepository.findByEmail("khach1@gmail.com")).thenReturn(Optional.of(testUser));

        User found = userService.findByEmail("khach1@gmail.com");

        assertThat(found.getEmail()).isEqualTo("khach1@gmail.com");
        assertThat(found.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(found.getIsActive()).isTrue();
        assertThat(found.getFullName()).isEqualTo("Le V?n Tam");
    }

    // 3.4 Login - blocked user is still returned by service layer (403 enforced in controller)
    @Test
    void findByEmail_BlockedUser_ReturnsUserWithActiveFalse() {
        // The service returns the entity regardless; AuthController enforces the 403.
        // This ensures the service doesn't accidentally filter out blocked users
        // (which would break the controller's ability to return a meaningful 403 message).
        when(userRepository.findByEmail("locked@gmail.com")).thenReturn(Optional.of(blockedUser));

        User found = userService.findByEmail("locked@gmail.com");

        assertThat(found.getEmail()).isEqualTo("locked@gmail.com");
        assertThat(found.getIsActive()).isFalse();
        assertThat(found.getRole()).isEqualTo(Role.ROLE_USER);
    }

    // 3.5 Change password - valid old and new password updates
    @Test
    void changePassword_ValidOldAndNew_UpdatesPassword() {
        when(userRepository.findByEmail("khach1@gmail.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("NewStrongPass1!")).thenReturn("$2a$12$newEncoded...");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // The PasswordValidator will be called - we need a valid password
        // "NewStrongPass1!" is 14 chars, meets criteria
        assertThatCode(() -> userService.changePassword(
                "khach1@gmail.com", "oldPassword", "NewStrongPass1!"))
                .doesNotThrowAnyException();

        verify(passwordEncoder).encode("NewStrongPass1!");
        verify(userRepository).save(testUser);
    }

    // 3.6 Forgot password - valid email generates token and sends email
    @Test
    void forgotPassword_ValidEmail_GeneratesToken() {
        when(userRepository.findByEmail("khach1@gmail.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$tempEncoded...");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));

        assertThatCode(() -> userService.requestPasswordReset("khach1@gmail.com"))
                .doesNotThrowAnyException();

        // Verify temp password was set and email was sent
        verify(passwordEncoder).encode(anyString());
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    // 3.7 Search users - by email returns matching
    @Test
    void searchUsers_ByEmail_ReturnsMatching() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);
        when(userRepository.searchUsers("khach1", pageable)).thenReturn(userPage);

        PagedResponse<UserResponse> response = userService.getAllUsers("khach1", pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1L);
        assertThat(response.getContent().get(0).email()).isEqualTo("khach1@gmail.com");
    }
}