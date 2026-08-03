package com.lottery.checker.dto.response;

import java.util.List;

public record CheckTicketResponse(
    CheckSummary summary,
    List<CheckDetail> details
) {
    public CheckTicketResponse {
        details = List.copyOf(details);
    }
    public record CheckSummary(long totalSpent, long totalWon) {}
    public record CheckDetail(String number, boolean isWon, String prize, long amount) {}
}
