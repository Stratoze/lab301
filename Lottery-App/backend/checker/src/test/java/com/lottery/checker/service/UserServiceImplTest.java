package com.lottery.checker.service;

import com.lottery.checker.dto.request.RegisterRequest;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.UserResponse;
import com.lottery.checker.entity.PasswordResetToken;
import com.lottery.checker.entity.Role;
import com.lottery.checker.entity.User;
import com.lottery.checker.exception.BadRequestException;
import com.lottery.checker.exception.ConflictException;
import com.lottery.checker.repository.PasswordResetTokenRepository;
import com.lottery.checker.repository.UserAuthProviderRepository;
import com.lottery.checker.repository.UserRepository;
import com.lottery.checker.service.impl.UserServiceImpl;
import com.lottery.checker.validation.PasswordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.lottery.checker.dto.request.ChangePasswordRequest;
import com.lottery.checker.dto.request.LinkSocialAccountRequest;
import com.lottery.checker.dto.request.UpdateUserRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthProviderRepository userAuthProviderRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SocialAuthService socialAuthService;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private UserCodeGenerator userCodeGenerator;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User adminUser;
    private User blockedUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(3L)
                .userCode("USR-10-2023-00000003")
                .email("khach1@gmail.com")
                .phone("0910000001")
                .password("$2a$12$hashedpassword...")
                .fullName("Le Van Tam")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        adminUser = User.builder()
                .id(1L)
                .userCode("USR-10-2023-00000001")
                .email("admin@veso.vn")
                .phone("0900000001")
                .password("$2a$12$hashedadmin...")
                .fullName("Phan Dang Duy Phuc")
                .role(Role.ROLE_ADMIN)
                .isActive(true)
                .build();

        blockedUser = User.builder()
                .id(9L)
                .userCode("USR-11-2023-00000001")
                .email("locked@gmail.com")
                .phone("0910000009")
                .password("$2a$12$hashedblocked...")
                .fullName("Nguoi Bi Khoa")
                .role(Role.ROLE_USER)
                .isActive(false)
                .build();
    }

    @Test
    void register_ValidUser_ReturnsCreatedUser() {
        RegisterRequest request = new RegisterRequest(
                "New User",
                "newuser@gmail.com",
                "0911111111",
                "StrongPass1!"
        );

        when(userRepository.findByEmail("newuser@gmail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.findByPhone("0911111111"))
                .thenReturn(Optional.empty());

        when(userCodeGenerator.generate()).thenReturn("USR-08-2026-00000016");

        when(passwordEncoder.encode("StrongPass1!"))
                .thenReturn("$2a$12$encoded...");

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

    @Test
    void register_DuplicateEmail_ThrowsConflict() {
        RegisterRequest request = new RegisterRequest(
                "Le Van Tam",
                "khach1@gmail.com",
                "0910000999",
                "StrongPass1!"
        );

        when(userRepository.findByEmail("khach1@gmail.com"))
                .thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_DuplicatePhone_ThrowsConflict() {
        RegisterRequest request = new RegisterRequest(
                "New User",
                "newuser@gmail.com",
                "0910000001",
                "StrongPass1!"
        );

        when(userRepository.findByEmail("newuser@gmail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.findByPhone("0910000001"))
                .thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Phone number already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void findByEmail_ActiveUser_ReturnsUserWithCorrectFields() {
        when(userRepository.findByEmail("khach1@gmail.com"))
                .thenReturn(Optional.of(testUser));

        User found = userService.findByEmail("khach1@gmail.com");

        assertThat(found.getEmail()).isEqualTo("khach1@gmail.com");
        assertThat(found.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(found.getIsActive()).isTrue();
        assertThat(found.getFullName()).isEqualTo("Le Van Tam");
    }

    @Test
    void findByEmail_BlockedUser_ReturnsUserWithActiveFalse() {
        when(userRepository.findByEmail("locked@gmail.com"))
                .thenReturn(Optional.of(blockedUser));

        User found = userService.findByEmail("locked@gmail.com");

        assertThat(found.getEmail()).isEqualTo("locked@gmail.com");
        assertThat(found.getIsActive()).isFalse();
        assertThat(found.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void changePassword_ValidOldAndNew_UpdatesPassword() {
        when(userRepository.findByEmail("khach1@gmail.com"))
                .thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches("oldPassword", testUser.getPassword()))
                .thenReturn(true);

        when(passwordEncoder.encode("NewStrongPass1!"))
                .thenReturn("$2a$12$newEncoded...");

        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        when(passwordValidator.isValid(eq("NewStrongPass1!"), any()))
                .thenReturn(true);

        assertThatCode(() -> userService.changePassword(
                "khach1@gmail.com",
                new ChangePasswordRequest("oldPassword", "NewStrongPass1!")
        )).doesNotThrowAnyException();

        verify(passwordEncoder).encode("NewStrongPass1!");
        verify(userRepository).save(testUser);
    }

    @Test
    void forgotPassword_ValidEmail_CreatesTokenAndSendsEmail() {
        when(userRepository.findByEmail("khach1@gmail.com"))
                .thenReturn(Optional.of(testUser));

        assertThatCode(() -> userService.requestPasswordReset("khach1@gmail.com"))
                .doesNotThrowAnyException();

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void forgotPassword_UnknownEmail_DoesNotSendEmail() {
        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        userService.requestPasswordReset("unknown@gmail.com");

        verify(passwordResetTokenRepository, never()).save(any());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void resetPassword_ValidToken_UpdatesPasswordAndMarksTokenUsed() {
        PasswordResetToken resetTokenEntity = PasswordResetToken.builder()
                .id(1L)
                .user(testUser)
                .token("valid-token")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(passwordResetTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(resetTokenEntity));

        when(passwordValidator.isValid(eq("NewStrongPass1!"), any()))
                .thenReturn(true);

        when(passwordEncoder.encode("NewStrongPass1!"))
                .thenReturn("$2a$12$newEncoded...");

        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenReturn(resetTokenEntity);

        userService.resetPassword("valid-token", "NewStrongPass1!");

        verify(passwordEncoder).encode("NewStrongPass1!");
        verify(userRepository).save(testUser);
        verify(passwordResetTokenRepository).save(resetTokenEntity);

        assertThat(resetTokenEntity.getIsUsed()).isTrue();
    }

    @Test
    void resetPassword_UsedToken_ThrowsError() {
        PasswordResetToken resetTokenEntity = PasswordResetToken.builder()
                .id(1L)
                .user(testUser)
                .token("used-token")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .isUsed(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(passwordResetTokenRepository.findByToken("used-token"))
                .thenReturn(Optional.of(resetTokenEntity));

        assertThatThrownBy(() -> userService.resetPassword("used-token", "NewStrongPass1!"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already been used");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_ExpiredToken_ThrowsError() {
        PasswordResetToken resetTokenEntity = PasswordResetToken.builder()
                .id(1L)
                .user(testUser)
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(passwordResetTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(resetTokenEntity));

        assertThatThrownBy(() -> userService.resetPassword("expired-token", "NewStrongPass1!"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid or expired reset token");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void searchUsers_ByEmail_ReturnsMatching() {
        Pageable pageable = PageRequest.of(0, 20);

        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(userPage);

        PagedResponse<UserResponse> response =
                userService.getAllUsers("khach1", pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1L);
        assertThat(response.getContent().get(0).email()).isEqualTo("khach1@gmail.com");
    }

    // --- changePassword: social-user first-set path ---

    @Test
    void changePassword_SocialUserFirstSet_SucceedsWithoutOldPassword() {
        User socialUser = User.builder()
                .id(5L).userCode("USR-10-2023-00000005")
                .email("social@gmail.com").password(null)
                .fullName("Social User").role(Role.ROLE_USER).isActive(true)
                .build();

        when(userRepository.findByEmail("social@gmail.com"))
                .thenReturn(Optional.of(socialUser));
        when(passwordValidator.isValid(eq("NewStrongPass1!"), any()))
                .thenReturn(true);
        when(passwordEncoder.encode("NewStrongPass1!"))
                .thenReturn("$2a$12$encoded...");
        when(userRepository.save(any(User.class))).thenReturn(socialUser);

        assertThatCode(() -> userService.changePassword(
                "social@gmail.com",
                new ChangePasswordRequest(null, "NewStrongPass1!")
        )).doesNotThrowAnyException();

        verify(passwordEncoder).encode("NewStrongPass1!");
        verify(userRepository).save(socialUser);
    }

    @Test
    void changePassword_SocialUserSendsOldPassword_ThrowsBadRequest() {
        User socialUser = User.builder()
                .id(5L).email("social@gmail.com").password(null)
                .fullName("Social User").role(Role.ROLE_USER).isActive(true)
                .build();

        when(userRepository.findByEmail("social@gmail.com"))
                .thenReturn(Optional.of(socialUser));

        assertThatThrownBy(() -> userService.changePassword(
                "social@gmail.com",
                new ChangePasswordRequest("someOldPass", "NewStrongPass1!")
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Leave current password empty");

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changePassword_WrongOldPassword_ThrowsBadRequest() {
        when(userRepository.findByEmail("khach1@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongOld", testUser.getPassword()))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(
                "khach1@gmail.com",
                new ChangePasswordRequest("wrongOld", "NewStrongPass1!")
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(passwordEncoder, never()).encode(any());
    }

    // --- updateUser ---

    @Test
    void updateUser_InvalidRole_ThrowsBadRequest() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.updateUser(
                3L, new UpdateUserRequest(null, null, "INVALID_ROLE", null)
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid role");
    }

    @Test
    void updateUser_NotFound_ThrowsNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(
                999L, new UpdateUserRequest("Name", null, null, null)
        ))
                .isInstanceOf(com.lottery.checker.exception.NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // --- sendBulkEmail validation ---

    @Test
    void sendBulkEmail_BlankSubject_ThrowsBadRequest() {
        assertThatThrownBy(() -> userService.sendBulkEmail(List.of(3L), "", "content"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("subject is required");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendBulkEmail_BlankContent_ThrowsBadRequest() {
        assertThatThrownBy(() -> userService.sendBulkEmail(List.of(3L), "Subject", ""))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("content is required");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // --- linkSocialAccount ---

    @Test
    void linkSocialAccount_AlreadyLinked_ThrowsConflict() {
        when(userRepository.findByEmail("khach1@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(userAuthProviderRepository.existsByUserAndProvider(testUser, "GOOGLE"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.linkSocialAccount(
                "khach1@gmail.com",
                new LinkSocialAccountRequest("GOOGLE", "some-token")
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already linked");

        verify(socialAuthService, never()).linkProvider(any(), any(), any());
    }

    // --- unlinkPhone ---

    @Test
    void unlinkPhone_SetsPhoneToNull() {
        when(userRepository.findByEmail("khach1@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.unlinkPhone("khach1@gmail.com");

        assertThat(testUser.getPhone()).isNull();
        verify(userRepository).save(testUser);
    }
}