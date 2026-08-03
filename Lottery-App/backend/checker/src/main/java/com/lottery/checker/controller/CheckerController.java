package com.lottery.checker.controller;

import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.service.CheckerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.lottery.checker.dto.request.CheckTicketsRequest;
import com.lottery.checker.dto.response.CheckTicketResponse;
import com.lottery.checker.exception.BadRequestException;
import com.lottery.checker.exception.ConflictException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/checker")
@RequiredArgsConstructor
public class CheckerController {

    private final CheckerService checkerService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<CheckTicketResponse>> checkTickets(
            @Valid @RequestBody CheckTicketsRequest request
    ) {
        if (request.date() != null && request.date().isAfter(LocalDate.now())) {
            throw new BadRequestException("Draw date cannot be in the future.");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String resolvedEmail = null;

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            resolvedEmail = auth.getName();
        }

        if (resolvedEmail == null && request.numbers().size() > 1) {
            throw new BadRequestException("Guests can only check one ticket at a time. Please login to check multiple tickets.");
        }

        // Reject duplicate numbers within the same request
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (String num : request.numbers()) {
            if (!seen.add(num.trim())) {
                throw new ConflictException("Duplicate ticket number in request: \"" + num.trim() + "\". Each ticket can only appear once.");
            }
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        checkerService.checkTickets(request, resolvedEmail)
                )
        );
    }

    @GetMapping("/available-dates")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getAvailableDates(
            @RequestParam Integer stationId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        checkerService.getAvailableDates(stationId)
                )
        );
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<com.lottery.checker.dto.response.HistorySessionResponse>>> getHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new SecurityException("Unauthorized request to user history.");
        }

        String email = auth.getName();

        return ResponseEntity.ok(
                ApiResponse.success(
                        checkerService.getUserHistory(email)
                )
        );
    }
}