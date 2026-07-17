package com.lottery.checker.dto.request;

import com.lottery.checker.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "Full name is required")
    String fullName,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    String phone,

    @ValidPassword
    String password
) {}