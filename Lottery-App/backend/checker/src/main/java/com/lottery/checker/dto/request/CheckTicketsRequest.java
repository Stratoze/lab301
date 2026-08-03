package com.lottery.checker.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;

public record CheckTicketsRequest(
    @NotNull(message = "Station ID is required")
    @Min(value = 1, message = "Invalid station selected")
    Integer stationId,

    @NotNull(message = "Draw date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date,

    @NotEmpty(message = "At least one ticket number is required")
    @Size(max = 50, message = "Maximum 50 tickets per check")
    List<@Pattern(regexp = "\\d{6}", message = "Invalid ticket number format. Must be exactly 6 digits") String> numbers
) {
    public CheckTicketsRequest {
        numbers = List.copyOf(numbers);
    }
}
