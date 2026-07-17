package com.lottery.checker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PrizeRequest(
    @NotBlank String type,
    @NotBlank String winningNumbers, // Can be multiple numbers separated by whitespace or special chars
    @NotNull Long rewardAmount
) {}