package com.lottery.checker.controller;

import com.lottery.checker.dto.request.CreateTicketRequest;
import com.lottery.checker.dto.request.UpdateTicketStatusRequest;
import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.TicketResponse;
import com.lottery.checker.entity.LotteryStation;
import com.lottery.checker.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {

    private final TicketService ticketService;

    @GetMapping("/stations")
    public ResponseEntity<ApiResponse<List<LotteryStation>>> getStations() {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getAllStations()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TicketResponse>>> getTickets(
            @RequestParam(required = false) Integer stationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "drawDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        ticketService.searchTickets(stationId, startDate, endDate, keyword, pageable)
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        ticketService.createTicket(request, principal.getName())
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody CreateTicketRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        ticketService.updateTicket(id, request, principal.getName())
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            Principal principal
    ) {
        ticketService.updateStatus(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully"));
    }
}