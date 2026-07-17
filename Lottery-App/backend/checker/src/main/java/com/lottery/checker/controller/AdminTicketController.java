package com.lottery.checker.controller;

import com.lottery.checker.dto.request.CreateTicketRequest;
import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.TicketResponse;
import com.lottery.checker.entity.LotteryStation;
import com.lottery.checker.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(ticketService.searchTickets(stationId, startDate, endDate, keyword, pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.createOrUpdateTicket(request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, String> payload) {
        ticketService.updateStatus(id, payload.get("status"));
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully"));
    }
}