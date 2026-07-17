package com.lottery.checker.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
    @NotBlank String provider, // "GOOGLE" or "FACEBOOK"
    @NotBlank String token     // ID token (Google) or access token (Facebook)
) {}