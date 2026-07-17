package com.lottery.checker.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record CreateTicketRequest(
    @NotNull Integer stationId,
    @NotNull LocalDate drawDate,
    @NotNull List<PrizeRequest> prizes,
    String status // PUBLISH or UNPUBLISH
) {}