package com.lottery.checker.dto.response;

public record PrizeResponse(
    String type,
    String winningNumber,
    Long rewardAmount
) {}