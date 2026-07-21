package com.lottery.checker.dto.response;

import java.util.List;

public record LinkedAccountsResponse(
    boolean googleLinked,
    boolean facebookLinked,
    String phone,
    boolean hasPassword,
    List<LinkedProvider> providers
) {
    public LinkedAccountsResponse {
        providers = List.copyOf(providers);
    }
    public record LinkedProvider(String provider, String providerId) {}
}