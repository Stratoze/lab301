package com.lottery.checker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SendEmailRequest(
    @NotEmpty(message = "User ids are required") List<Long> ids,
    @NotBlank(message = "Subject is required") String subject,
    @NotBlank(message = "Content is required") String content
) {
    public SendEmailRequest {
        ids = List.copyOf(ids);
    }
}