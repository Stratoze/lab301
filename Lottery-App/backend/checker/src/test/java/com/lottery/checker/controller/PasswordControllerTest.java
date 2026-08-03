package com.lottery.checker.controller;

import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import com.lottery.checker.dto.request.ForgotPasswordRequest;
import com.lottery.checker.dto.request.ResetPasswordRequest;
import com.lottery.checker.exception.BadRequestException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private PasswordController passwordController;

    // --- CN1: Forgot password ---

    @Test
    void forgotPassword_ValidEmail_ReturnsGenericSuccess() {
        doNothing().when(userService).requestPasswordReset("khach1@gmail.com");

        ResponseEntity<ApiResponse<String>> response =
                passwordController.forgotPassword(new ForgotPasswordRequest("khach1@gmail.com"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().isSuccess()).isTrue();
        // Generic message — does not reveal whether email exists
        assertThat(response.getBody().getData()).contains("If the email exists");
        verify(userService).requestPasswordReset("khach1@gmail.com");
    }

    @Test
    void forgotPassword_UnknownEmail_StillReturnsSuccess() {
        // Service silently ignores unknown emails (no exception)
        doNothing().when(userService).requestPasswordReset("unknown@gmail.com");

        ResponseEntity<ApiResponse<String>> response =
                passwordController.forgotPassword(new ForgotPasswordRequest("unknown@gmail.com"));

        // Same generic response — prevents email enumeration
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).contains("If the email exists");
    }

    // --- CN1: Reset password ---

    @Test
    void resetPassword_ValidToken_ReturnsSuccess() {
        doNothing().when(userService).resetPassword("valid-token", "NewStrongPass1!");

        ResponseEntity<ApiResponse<String>> response =
                passwordController.resetPassword(
                        new ResetPasswordRequest("valid-token", "NewStrongPass1!"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).contains("reset successfully");
        verify(userService).resetPassword("valid-token", "NewStrongPass1!");
    }

    @Test
    void resetPassword_InvalidToken_ServiceThrowsException() {
        doThrow(new BadRequestException("Invalid or expired reset token"))
                .when(userService).resetPassword("bad-token", "NewStrongPass1!");

        // Controller lets the exception propagate to GlobalExceptionHandler
        org.junit.jupiter.api.Assertions.assertThrows(BadRequestException.class, () ->
                passwordController.resetPassword(
                        new ResetPasswordRequest("bad-token", "NewStrongPass1!")));
    }

    // --- CN1: Validate reset token ---

    @Test
    void validateResetToken_ValidToken_ReturnsTrue() {
        when(userService.isResetTokenValid("valid-token")).thenReturn(true);

        ResponseEntity<ApiResponse<Map<String, Boolean>>> response =
                passwordController.validateResetToken("valid-token");

        assertThat(response.getBody().getData()).containsEntry("valid", true);
    }

    @Test
    void validateResetToken_ExpiredToken_ReturnsFalse() {
        when(userService.isResetTokenValid("expired-token")).thenReturn(false);

        ResponseEntity<ApiResponse<Map<String, Boolean>>> response =
                passwordController.validateResetToken("expired-token");

        assertThat(response.getBody().getData()).containsEntry("valid", false);
    }

    @Test
    void validateResetToken_UsedToken_ReturnsFalse() {
        when(userService.isResetTokenValid("used-token")).thenReturn(false);

        ResponseEntity<ApiResponse<Map<String, Boolean>>> response =
                passwordController.validateResetToken("used-token");

        assertThat(response.getBody().getData()).containsEntry("valid", false);
    }
}