package com.lottery.checker.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record HistorySessionResponse(
    Long id,
    LocalDateTime date,
    long totalSpent,
    long totalWon,
    List<HistoryTicketResponse> tickets
) {
    public HistorySessionResponse {
        tickets = List.copyOf(tickets);
    }
    public record HistoryTicketResponse(
        String number,
        boolean isWon,
        String prize,
        long amount,
        String station,
        LocalDate drawDate,
        LocalDateTime checkTime
    ) {}
}
