package com.lottery.checker.controller;

import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.LinkedAccountsResponse;
import com.lottery.checker.dto.response.UserResponse;
import com.lottery.checker.entity.Role;
import com.lottery.checker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.lottery.checker.dto.request.UpdateMeRequest;
import com.lottery.checker.dto.request.ChangePasswordRequest;
import com.lottery.checker.dto.request.LinkSocialAccountRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Principal principal;

    @InjectMocks
    private UserController userController;

    private UserResponse testUser;

    @BeforeEach
    void setUp() {
        when(principal.getName()).thenReturn("khach1@gmail.com");
        testUser = new UserResponse(
                3L, "USR-10-2023-00000003", "khach1@gmail.com",
                "0910000001", "Le Van Tam", Role.ROLE_USER,
                true, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // --- CN1: Get current user profile ---

    @Test
    void getMe_ValidUser_ReturnsUserProfile() {
        when(userService.getMe("khach1@gmail.com")).thenReturn(testUser);

        ResponseEntity<ApiResponse<UserResponse>> response =
                userController.getMe(principal);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getData().email()).isEqualTo("khach1@gmail.com");
        assertThat(response.getBody().getData().fullName()).isEqualTo("Le Van Tam");
        assertThat(response.getBody().getData().userCode()).isEqualTo("USR-10-2023-00000003");
    }

    // --- CN1: Update profile ---

    @Test
    void updateMe_ChangeFullName_ReturnsUpdated() {
        UserResponse updated = new UserResponse(
                3L, "USR-10-2023-00000003", "khach1@gmail.com",
                "0910000001", "New Name", Role.ROLE_USER,
                true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(userService.updateMe(eq("khach1@gmail.com"), any(UpdateMeRequest.class))).thenReturn(updated);

        ResponseEntity<ApiResponse<UserResponse>> response =
                userController.updateMe(principal, new UpdateMeRequest("New Name", null));

        assertThat(response.getBody().getData().fullName()).isEqualTo("New Name");
    }

    // --- CN1: Change password ---

    @Test
    void changePassword_ValidRequest_ReturnsSuccess() {
        doNothing().when(userService)
                .changePassword(eq("khach1@gmail.com"), any(ChangePasswordRequest.class));

        ResponseEntity<ApiResponse<String>> response =
                userController.changePassword(principal,
                        new ChangePasswordRequest("oldPass", "newPass"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(userService).changePassword(eq("khach1@gmail.com"), any(ChangePasswordRequest.class));
    }

    // --- CN1: Linked accounts ---

    @Test
    void getLinkedAccounts_ReturnsProviderStatus() {
        LinkedAccountsResponse linked = new LinkedAccountsResponse(
                true, false, "0910000001", true,
                List.of(new LinkedAccountsResponse.LinkedProvider("GOOGLE", "GOO_001"))
        );
        when(userService.getLinkedAccounts("khach1@gmail.com")).thenReturn(linked);

        ResponseEntity<ApiResponse<LinkedAccountsResponse>> response =
                userController.getLinkedAccounts(principal);

        assertThat(response.getBody().getData().googleLinked()).isTrue();
        assertThat(response.getBody().getData().facebookLinked()).isFalse();
        assertThat(response.getBody().getData().hasPassword()).isTrue();
    }

    // --- CN1: Link social account ---

    @Test
    void linkSocialAccount_ValidProvider_ReturnsSuccess() {
        doNothing().when(userService)
                .linkSocialAccount(eq("khach1@gmail.com"), any(LinkSocialAccountRequest.class));

        ResponseEntity<ApiResponse<String>> response =
                userController.linkSocialAccount(principal,
                        new LinkSocialAccountRequest("GOOGLE", "mock-token"));

        assertThat(response.getBody().isSuccess()).isTrue();
        verify(userService).linkSocialAccount(eq("khach1@gmail.com"), any(LinkSocialAccountRequest.class));
    }

    // --- CN1: Unlink phone ---

    @Test
    void unlinkPhone_ReturnsSuccess() {
        doNothing().when(userService).unlinkPhone("khach1@gmail.com");

        ResponseEntity<ApiResponse<String>> response =
                userController.unlinkPhone(principal);

        assertThat(response.getBody().isSuccess()).isTrue();
        verify(userService).unlinkPhone("khach1@gmail.com");
    }
}