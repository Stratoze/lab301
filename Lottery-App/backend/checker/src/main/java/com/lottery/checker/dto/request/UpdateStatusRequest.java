package com.lottery.checker.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateStatusRequest(
        @NotEmpty(message = "User ids are required") List<Long> ids,
        @NotNull(message = "isActive is required") Boolean isActive
) {}