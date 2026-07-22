package com.lottery.checker.service;

import com.lottery.checker.dto.request.CreateTicketRequest;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.TicketResponse;
import com.lottery.checker.entity.LotteryStation;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TicketService {

    List<LotteryStation> getAllStations();

    TicketResponse createTicket(CreateTicketRequest request, String adminEmail);

    TicketResponse updateTicket(Long id, CreateTicketRequest request, String adminEmail);

    PagedResponse<TicketResponse> searchTickets(
            Integer stationId,
            LocalDate start,
            LocalDate end,
            String keyword,
            Pageable pageable
    );

    void updateStatus(Long id, String status);

    void updateStatus(Long id, String status, String adminEmail);
}