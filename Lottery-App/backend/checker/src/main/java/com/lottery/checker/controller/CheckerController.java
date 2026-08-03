package com.lottery.checker.controller;

import com.lottery.checker.dto.request.CheckTicketsRequest;
import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.CheckTicketResponse;
import com.lottery.checker.dto.response.HistorySessionResponse;
import com.lottery.checker.service.CheckerService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checker")
@RequiredArgsConstructor
public class CheckerController {

    private final CheckerService checkerService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<CheckTicketResponse>> checkTickets(
            @Valid @RequestBody CheckTicketsRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        checkerService.checkTickets(request, resolveEmail(authentication))
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
    public ResponseEntity<ApiResponse<List<HistorySessionResponse>>> getHistory(Principal principal) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        checkerService.getUserHistory(principal.getName())
                )
        );
    }

    private static String resolveEmail(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }
}