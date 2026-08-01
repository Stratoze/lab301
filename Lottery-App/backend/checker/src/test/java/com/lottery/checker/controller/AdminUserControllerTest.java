package com.lottery.checker.controller;

import com.lottery.checker.dto.request.SendEmailRequest;
import com.lottery.checker.dto.request.UpdateStatusRequest;
import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.UserResponse;
import com.lottery.checker.entity.Role;
import com.lottery.checker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    private UserResponse activeUser;
    private UserResponse blockedUser;
    private UserResponse adminUser;

    @BeforeEach
    void setUp() {
        activeUser = new UserResponse(
                3L, "USR-10-2023-00000003", "khach1@gmail.com",
                "0910000001", "Le Van Tam", Role.ROLE_USER,
                true, LocalDateTime.now(), LocalDateTime.now()
        );
        blockedUser = new UserResponse(
                9L, "USR-11-2023-00000001", "locked@gmail.com",
                "0910000009", "Nguoi Bi Khoa", Role.ROLE_USER,
                false, null, LocalDateTime.now()
        );
        adminUser = new UserResponse(
                1L, "USR-10-2023-00000001", "admin@veso.vn",
                "0900000001", "Phan Dang Duy Phuc", Role.ROLE_ADMIN,
                true, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // --- CN1: Admin search + pagination ---

    @Test
    void getUsers_WithKeyword_ReturnsFilteredPage() {
        PagedResponse<UserResponse> page = PagedResponse.<UserResponse>builder()
                .content(List.of(activeUser))
                .page(0).size(20).totalElements(1).totalPages(1).last(true)
                .build();
        when(userService.getAllUsers(eq("khach1"), isNull(), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> response =
                adminUserController.getUsers("khach1", null, 0, 20);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getData().getContent()).hasSize(1);
        assertThat(response.getBody().getData().getContent().get(0).email())
                .isEqualTo("khach1@gmail.com");
    }

    @Test
    void getUsers_WithLoginFilter_ReturnsInactiveUsers() {
        PagedResponse<UserResponse> page = PagedResponse.<UserResponse>builder()
                .content(List.of(blockedUser))
                .page(0).size(20).totalElements(1).totalPages(1).last(true)
                .build();
        when(userService.getAllUsers(isNull(), eq("inactive-1m"), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> response =
                adminUserController.getUsers(null, "inactive-1m", 0, 20);

        assertThat(response.getBody().getData().getContent()).hasSize(1);
        assertThat(response.getBody().getData().getContent().get(0).isActive()).isFalse();
    }

    @Test
    void getUsers_DefaultPagination_UsesPage0Size20() {
        PagedResponse<UserResponse> page = PagedResponse.<UserResponse>builder()
                .content(List.of(activeUser, adminUser))
                .page(0).size(20).totalElements(2).totalPages(1).last(true)
                .build();
        when(userService.getAllUsers(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> response =
                adminUserController.getUsers(null, null, 0, 20);

        assertThat(response.getBody().getData().getContent()).hasSize(2);
        verify(userService).getAllUsers(isNull(), isNull(), any(Pageable.class));
    }

    // --- CN1: Bulk status update ---

    @Test
    void updateStatus_BulkBlock_CallsServiceWithIdsAndFalse() {
        UpdateStatusRequest request = new UpdateStatusRequest(List.of(3L, 4L, 5L), false);

        ResponseEntity<ApiResponse<String>> response =
                adminUserController.updateStatus(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(userService).updateStatus(List.of(3L, 4L, 5L), false);
    }

    @Test
    void updateStatus_BulkUnlock_CallsServiceWithIdsAndTrue() {
        UpdateStatusRequest request = new UpdateStatusRequest(List.of(9L), true);

        ResponseEntity<ApiResponse<String>> response =
                adminUserController.updateStatus(request);

        verify(userService).updateStatus(List.of(9L), true);
        assertThat(response.getBody().getData()).contains("successfully");
    }

    // --- CN1: Update user (role, name, status) ---

    @Test
    void updateUser_ChangeRole_ReturnsUpdatedUser() {
        UserResponse updated = new UserResponse(
                3L, "USR-10-2023-00000003", "khach1@gmail.com",
                "0910000001", "Le Van Tam", Role.ROLE_ADMIN,
                true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(userService.updateUser(3L, null, null, Role.ROLE_ADMIN, null))
                .thenReturn(updated);

        Map<String, Object> payload = Map.of("role", "ROLE_ADMIN");
        ResponseEntity<ApiResponse<UserResponse>> response =
                adminUserController.updateUser(3L, payload);

        assertThat(response.getBody().getData().role()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void updateUser_ChangeNameAndStatus_ReturnsUpdated() {
        UserResponse updated = new UserResponse(
                3L, "USR-10-2023-00000003", "khach1@gmail.com",
                "0910000001", "New Name", Role.ROLE_USER,
                false, LocalDateTime.now(), LocalDateTime.now()
        );
        when(userService.updateUser(3L, "New Name", null, null, false))
                .thenReturn(updated);

        Map<String, Object> payload = Map.of("fullName", "New Name", "isActive", false);
        ResponseEntity<ApiResponse<UserResponse>> response =
                adminUserController.updateUser(3L, payload);

        assertThat(response.getBody().getData().fullName()).isEqualTo("New Name");
        assertThat(response.getBody().getData().isActive()).isFalse();
    }

    // --- CN1: Send email ---

    @Test
    void sendEmail_ValidRequest_CallsServiceAndReturnsSuccess() {
        SendEmailRequest request = new SendEmailRequest(
                List.of(3L, 4L), "Welcome back!", "We miss you."
        );

        ResponseEntity<ApiResponse<String>> response =
                adminUserController.sendEmail(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(userService).sendBulkEmail(List.of(3L, 4L), "Welcome back!", "We miss you.");
    }

    // --- CN1: Export ---

    @Test
    void exportUsers_CsvFormat_ReturnsCsvContentType() {
        when(userService.getAllUsers(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(PagedResponse.<UserResponse>builder()
                        .content(List.of(activeUser))
                        .page(0).size(20).totalElements(1).totalPages(1).last(true)
                        .build());

        ResponseEntity<?> response =
                adminUserController.exportUsers(null, "csv", null);

        assertThat(response.getHeaders().getContentType().toString())
                .contains("text/csv");
        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .isEqualTo("users_export.csv");
    }

    @Test
    void exportUsers_ExcelFormat_ReturnsXlsxContentType() {
        when(userService.getAllUsers(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(PagedResponse.<UserResponse>builder()
                        .content(List.of(activeUser))
                        .page(0).size(20).totalElements(1).totalPages(1).last(true)
                        .build());

        ResponseEntity<?> response =
                adminUserController.exportUsers(null, "excel", null);

        assertThat(response.getHeaders().getContentType().toString())
                .contains("spreadsheetml");
    }

    @Test
    void exportUsers_JsonFormat_ReturnsJsonBody() {
        when(userService.getAllUsers(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(PagedResponse.<UserResponse>builder()
                        .content(List.of(activeUser))
                        .page(0).size(20).totalElements(1).totalPages(1).last(true)
                        .build());

        ResponseEntity<?> response =
                adminUserController.exportUsers(null, "json", null);

        assertThat(response.getHeaders().getContentType().toString())
                .contains("application/json");
    }

    @Test
    void exportUsers_WithSpecificIds_UsesGetUsersByIds() {
        when(userService.getUsersByIds(List.of(3L, 9L)))
                .thenReturn(List.of(activeUser, blockedUser));

        ResponseEntity<?> response =
                adminUserController.exportUsers(null, "csv", List.of(3L, 9L));

        verify(userService).getUsersByIds(List.of(3L, 9L));
        verify(userService, never()).getAllUsers(any(), any(), any());
    }
}