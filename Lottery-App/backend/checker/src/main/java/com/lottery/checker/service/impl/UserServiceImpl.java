package com.lottery.checker.service.impl;

import com.lottery.checker.dto.request.RegisterRequest;
import com.lottery.checker.dto.response.LinkedAccountsResponse;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.UserResponse;
import com.lottery.checker.entity.Role;
import com.lottery.checker.entity.User;
import com.lottery.checker.entity.UserAuthProvider;
import com.lottery.checker.repository.PasswordResetTokenRepository;
import com.lottery.checker.repository.UserAuthProviderRepository;
import com.lottery.checker.repository.UserRepository;
import com.lottery.checker.service.SocialAuthService;
import com.lottery.checker.service.UserService;
import com.lottery.checker.validation.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAuthProviderRepository userAuthProviderRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;
    private final SocialAuthService socialAuthService;
    private final PasswordValidator passwordValidator;

    private static final AtomicLong userCounter = new AtomicLong(16);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .userCode(generateUserCode())
                .email(request.email())
                .fullName(request.fullName())
                .phone(request.phone())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .isActive(true)
                .lastLogin(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public PagedResponse<UserResponse> getAllUsers(String keyword, Pageable pageable) {
        Page<User> userPage;
        if (keyword != null && !keyword.isBlank()) {
            userPage = userRepository.searchUsers(keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserResponse> content = userPage.getContent().stream()
                .map(UserResponse::fromEntity)
                .toList();

        return PagedResponse.<UserResponse>builder()
                .content(content)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void updateStatus(List<Long> ids, boolean isActive) {
        List<User> users = userRepository.findAllById(ids);
        users.forEach(user -> user.setIsActive(isActive));
        userRepository.saveAll(users);
    }

    @Override
    public List<UserResponse> getUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, String fullName, String phone, Role role, Boolean isActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (fullName != null) user.setFullName(fullName);
        if (phone != null) user.setPhone(phone);
        if (role != null) user.setRole(role);
        if (isActive != null) user.setIsActive(isActive);
        
        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    public UserResponse getMe(String email) {
        User user = findByEmail(email);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse updateMe(String email, Map<String, String> updates) {
        User user = findByEmail(email);
        if (updates.containsKey("fullName") && updates.get("fullName") != null) {
            user.setFullName(updates.get("fullName"));
        }
        if (updates.containsKey("phone")) {
            String phone = updates.get("phone");
            // Validate phone format (optional, but ensure it's not empty string treated as null)
            if (phone != null && !phone.isBlank()) {
                if (!phone.matches("^\\d{7,15}$")) {
                    throw new RuntimeException("Invalid phone number format");
                }
                user.setPhone(phone);
            } else if (phone != null && phone.isBlank()) {
                // Allow clearing phone via empty string
                user.setPhone(null);
            }
        }
        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = findByEmail(email);
        
        boolean isSettingInitialPassword = user.getPassword() == null;
        
        if (isSettingInitialPassword) {
            // Social user setting password for the first time: no old password required
            if (oldPassword != null && !oldPassword.isBlank()) {
                throw new RuntimeException("Account has no password set. Leave current password empty to set a new one.");
            }
        } else {
            // Existing password user: must provide correct current password
            if (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
        }

        // Validate new password using same rules as registration
        if (!passwordValidator.isValid(newPassword, null)) {
            throw new RuntimeException("New password does not meet security requirements. " +
                "It must be 10-64 characters and not contain common words or patterns.");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = findByEmail(email);
        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Your New Temporary Password");
        message.setText(String.format(
            "Hello %s,%n%n" +
            "Your temporary password is: %s%n%n" +
            "Please log in and change it immediately.%n%n" +
            "? Lottery System",
            user.getFullName(), tempPassword
        ));
        mailSender.send(message);
    }

    @Override
    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        // Token-based reset is no longer used; temporary password sent via email instead.
        throw new UnsupportedOperationException("Use 'Forgot Password' to get a new temporary password.");
    }

    @Override
    @Transactional
    public void updateLastLogin(String email) {
        User user = findByEmail(email);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public LinkedAccountsResponse getLinkedAccounts(String email) {
        User user = findByEmail(email);
        List<UserAuthProvider> providers = userAuthProviderRepository.findByUser(user);
        return new LinkedAccountsResponse(
            providers.stream().anyMatch(p -> "GOOGLE".equals(p.getProvider())),
            providers.stream().anyMatch(p -> "FACEBOOK".equals(p.getProvider())),
            user.getPhone(),
            user.getPassword() != null,
            providers.stream()
                .map(p -> new LinkedAccountsResponse.LinkedProvider(p.getProvider(), p.getProviderId()))
                .toList()
        );
    }

    @Override
    @Transactional
    public void linkSocialAccount(String email, String provider, String token) {
        User user = findByEmail(email);
        boolean alreadyLinked = userAuthProviderRepository.existsByUserAndProvider(user, provider.toUpperCase());
        if (alreadyLinked) {
            throw new RuntimeException("Account already linked to " + provider);
        }
        // Verify token and link
        socialAuthService.linkProvider(user, provider.toUpperCase(), token);
    }

    @Override
    @Transactional
    public void unlinkPhone(String email) {
        User user = findByEmail(email);
        user.setPhone(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void sendBulkEmail(List<Long> ids, String subject, String content) {
        List<User> users = userRepository.findAllById(ids);
        for (User user : users) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(String.format(
                "Hello %s,%n%n%s%n%n? Lottery System",
                user.getFullName(), content
            ));
            mailSender.send(message);
        }
    }

    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    private String generateUserCode() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        return String.format("USR-%s-%08d", datePart, userCounter.getAndIncrement());
    }
}