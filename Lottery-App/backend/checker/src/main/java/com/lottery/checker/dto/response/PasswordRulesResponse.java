package com.lottery.checker.dto.response;

import java.util.Set;

public record PasswordRulesResponse(
    int minLength,
    int maxLength,
    Set<String> blocklist
) {
    public PasswordRulesResponse {
        blocklist = Set.copyOf(blocklist);
    }
}