package com.lottery.checker.controller;

import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.service.CheckerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/checker")
@RequiredArgsConstructor
public class CheckerController {

    private final CheckerService checkerService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkTickets(
            @RequestParam Integer stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody List<String> numbers) {

        // Validate stationId is positive
        if (stationId == null || stationId <= 0) {
            throw new RuntimeException("Invalid station selected.");
        }

        // Validate date is not in the future
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new RuntimeException("Draw date cannot be in the future.");
        }

        // Validate ticket numbers
        if (numbers == null || numbers.isEmpty()) {
            throw new RuntimeException("At least one ticket number is required.");
        }

        for (String num : numbers) {
            String trimmed = num.trim();
            if (trimmed.isEmpty()) {
                throw new RuntimeException("Ticket numbers cannot be empty.");
            }
            if (!trimmed.matches("\\d{6}")) {
                throw new RuntimeException("Invalid ticket number: \"" + trimmed + "\". Each ticket must be exactly 6 digits.");
            }
        }

        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String resolvedEmail = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            resolvedEmail = auth.getName();
        }

        return ResponseEntity.ok(ApiResponse.success(checkerService.checkTickets(stationId, date, numbers, resolvedEmail)));
    }

    @GetMapping("/available-dates")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getAvailableDates(
            @RequestParam Integer stationId) {
        return ResponseEntity.ok(ApiResponse.success(checkerService.getAvailableDates(stationId)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHistory() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Unauthorized request to user history.");
        }
        String email = auth.getName();
        return ResponseEntity.ok(ApiResponse.success(checkerService.getUserHistory(email)));
    }
}