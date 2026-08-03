package com.lottery.checker.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record CreateTicketRequest(
    @NotNull Integer stationId,
    @NotNull LocalDate drawDate,
    @NotNull @Valid List<PrizeRequest> prizes,
    String status // PUBLISH or UNPUBLISH
) {
    public CreateTicketRequest {
        prizes = List.copyOf(prizes);
    }
}