package com.lottery.checker.service.impl;

import com.lottery.checker.exception.BadRequestException;
import com.lottery.checker.exception.ConflictException;
import com.lottery.checker.exception.NotFoundException;

import com.lottery.checker.dto.request.RegisterRequest;
import com.lottery.checker.dto.response.LinkedAccountsResponse;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.UserResponse;
import com.lottery.checker.entity.PasswordResetToken;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAuthProviderRepository userAuthProviderRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;
    private final SocialAuthService socialAuthService;
    private final PasswordValidator passwordValidator;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("Email already exists");
        }

        if (request.phone() != null && !request.phone().isBlank()) {
            String phone = request.phone().trim();

            if (!phone.matches("^\\d{7,15}$")) {
                throw new BadRequestException("Invalid phone number format");
            }

            if (userRepository.findByPhone(phone).isPresent()) {
                throw new ConflictException("Phone number already exists");
            }
        }

        User user = User.builder()
                .userCode(generateUserCode())
                .email(request.email())
                .fullName(request.fullName())
                .phone(request.phone() != null && !request.phone().isBlank() ? request.phone().trim() : null)
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
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public Optional<User> findByEmailOptional(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public PagedResponse<UserResponse> getAllUsers(String keyword, Pageable pageable) {
        return getAllUsers(keyword, null, pageable);
    }

    @Override
    public PagedResponse<UserResponse> getAllUsers(String keyword, String loginFilter, Pageable pageable) {
        Page<User> userPage = userRepository.findAll(userSpecification(keyword, loginFilter), pageable);

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

    private Specification<User> userSpecification(String keyword, String loginFilter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String lowerKeyword = keyword.toLowerCase();
                String likePattern = "%" + lowerKeyword + "%";

                Predicate keywordPredicate = cb.or(
                        cb.like(cb.lower(root.get("fullName")), likePattern),
                        cb.like(cb.lower(root.get("email")), likePattern),
                        cb.like(cb.lower(root.get("phone")), likePattern),
                        cb.like(cb.lower(root.get("userCode")), likePattern)
                );

                predicates.add(keywordPredicate);
            }

            if (loginFilter != null && !loginFilter.isBlank()) {
                boolean inactive = loginFilter.startsWith("inactive-");
                String period = inactive ? loginFilter.substring("inactive-".length()) : loginFilter;

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime cutoff = switch (period) {
                    case "24h" -> now.minusHours(24);
                    case "1w" -> now.minusWeeks(1);
                    case "1m" -> now.minusMonths(1);
                    case "3m" -> now.minusMonths(3);
                    case "6m" -> now.minusMonths(6);
                    case "1y" -> now.minusYears(1);
                    default -> null;
                };

                if (cutoff != null) {
                    if (inactive) {
                        predicates.add(cb.or(
                                cb.isNull(root.get("lastLogin")),
                                cb.lessThan(root.get("lastLogin"), cutoff)
                        ));
                    } else {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("lastLogin"), cutoff));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
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
    public UserResponse updateUser(Long id, com.lottery.checker.dto.request.UpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (req.fullName() != null && !req.fullName().isBlank()) {
            user.setFullName(req.fullName());
        }

        if (req.phone() != null) {
            if (!req.phone().isBlank()) {
                if (!req.phone().matches("^\\d{7,15}$")) {
                    throw new BadRequestException("Invalid phone number format");
                }

                userRepository.findByPhone(req.phone())
                        .filter(existing -> !existing.getId().equals(id))
                        .ifPresent(existing -> {
                            throw new ConflictException("Phone number already exists");
                        });

                user.setPhone(req.phone());
            } else {
                user.setPhone(null);
            }
        }

        if (req.role() != null && !req.role().isBlank()) {
            try {
                user.setRole(Role.valueOf(req.role()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role specified");
            }
        }

        if (req.isActive() != null) {
            user.setIsActive(req.isActive());
        }

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    public UserResponse getMe(String email) {
        User user = findByEmail(email);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse updateMe(String email, com.lottery.checker.dto.request.UpdateMeRequest req) {
        User user = findByEmail(email);

        if (req.fullName() != null) {
            user.setFullName(req.fullName());
        }

        if (req.phone() != null) {
            String phone = req.phone();

            if (!phone.isBlank()) {
                if (!phone.matches("^\\d{7,15}$")) {
                    throw new BadRequestException("Invalid phone number format");
                }

                userRepository.findByPhone(phone)
                        .filter(existing -> !existing.getId().equals(user.getId()))
                        .ifPresent(existing -> {
                            throw new ConflictException("Phone number already exists");
                        });

                user.setPhone(phone);
            } else {
                user.setPhone(null);
            }
        }

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(String email, com.lottery.checker.dto.request.ChangePasswordRequest req) {
        User user = findByEmail(email);

        boolean isSettingInitialPassword = user.getPassword() == null;

        if (isSettingInitialPassword) {
            if (req.oldPassword() != null && !req.oldPassword().isBlank()) {
                throw new BadRequestException("Account has no password set. Leave current password empty to set a new one.");
            }
        } else {
            if (req.oldPassword() == null || !passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }
        }

        if (!passwordValidator.isValid(req.newPassword(), null)) {
            throw new BadRequestException("New password does not meet security requirements. It must be 10-64 characters and not contain common words or patterns.");
        }

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            // Do not reveal whether the email exists.
            return;
        }

        User user = userOptional.get();
        String resetCode = UUID.randomUUID().toString();

        PasswordResetToken resetTokenEntity = PasswordResetToken.builder()
                .user(user)
                .token(resetCode)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();

        passwordResetTokenRepository.save(resetTokenEntity);

        String resetLink = frontendBaseUrl + "/reset-password?" + "token" + "=" + resetCode;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset Your Lottery System Password");
        message.setText(
                "Hello " + user.getFullName() + ",\n\n" +
                "Click the link below to reset your password:\n\n" +
                resetLink + "\n\n" +
                "If the link does not work, open this page in your browser:\n" +
                frontendBaseUrl + "/reset-password\n\n" +
                "Then enter this reset code manually:\n" +
                resetCode + "\n\n" +
                "This link expires in 1 hour.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Lottery System"
        );

        mailSender.send(message);
    }

    @Override
    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        if (tokenValue == null || tokenValue.isBlank()) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (Boolean.TRUE.equals(token.getIsUsed())) {
            throw new ConflictException("This reset link has already been used");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        if (!passwordValidator.isValid(newPassword, null)) {
            throw new BadRequestException("New password does not meet security requirements. It must be 10-64 characters and not contain common words or patterns.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setIsUsed(true);
        passwordResetTokenRepository.save(token);
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
    public void linkSocialAccount(String email, com.lottery.checker.dto.request.LinkSocialAccountRequest req) {
        User user = findByEmail(email);

        boolean alreadyLinked = userAuthProviderRepository.existsByUserAndProvider(user, req.provider().toUpperCase());

        if (alreadyLinked) {
            throw new ConflictException("Account already linked to " + req.provider());
        }

        socialAuthService.linkProvider(user, req.provider().toUpperCase(), req.token());
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
        if (subject == null || subject.isBlank()) {
            throw new BadRequestException("Email subject is required");
        }

        if (content == null || content.isBlank()) {
            throw new BadRequestException("Email content is required");
        }

        List<User> users = userRepository.findAllById(ids);

        for (User user : users) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(String.format(
                    "Hello %s,%n%n%s%n%n– Lottery System",
                    user.getFullName(),
                    content
            ));

            mailSender.send(message);
        }
    }

    @Override
    public boolean isResetTokenValid(String resetCode) {
        if (resetCode == null || resetCode.isBlank()) {
            return false;
        }

        return passwordResetTokenRepository.findByToken(resetCode)
                .map(resetTokenEntity ->
                        !Boolean.TRUE.equals(resetTokenEntity.getIsUsed())
                        && resetTokenEntity.getExpiresAt().isAfter(LocalDateTime.now())
                )
                .orElse(false);
    }

    private String generateUserCode() {
        LocalDateTime now = LocalDateTime.now();
        String monthYear = now.format(DateTimeFormatter.ofPattern("MM-yyyy"));

        long count = userRepository.countUsersByMonth(monthYear);
        String code;

        do {
            count++;
            code = String.format("USR-%s-%08d", monthYear, count);
        } while (userRepository.findByUserCode(code).isPresent());

        return code;
    }
}