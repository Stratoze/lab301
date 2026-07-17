package com.lottery.checker.dto.response;

import com.lottery.checker.entity.LotteryResult;
import java.time.LocalDate;
import java.util.List;

public record TicketResponse(
    Long id,
    String resultCode,
    String stationName,
    LocalDate drawDate,
    String status,
    Long totalQueries,
    List<PrizeResponse> prizes
) {
    public static TicketResponse fromEntity(LotteryResult result) {
        List<PrizeResponse> prizeList = result.getPrizeDetails().stream()
                .map(p -> new PrizeResponse(p.getPrizeType(), p.getWinningNumber(), p.getRewardAmount()))
                .toList();

        return new TicketResponse(
            result.getId(),
            result.getResultCode(),
            result.getStation().getName(),
            result.getDrawDate(),
            result.getStatus(),
            result.getTotalQueries(),
            prizeList
        );
    }
}