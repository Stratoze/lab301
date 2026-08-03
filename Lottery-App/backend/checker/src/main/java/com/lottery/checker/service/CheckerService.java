package com.lottery.checker.service;

import java.time.LocalDate;
import java.util.List;

public interface CheckerService {
    com.lottery.checker.dto.response.CheckTicketResponse checkTickets(com.lottery.checker.dto.request.CheckTicketsRequest request, String userEmail);
    List<com.lottery.checker.dto.response.HistorySessionResponse> getUserHistory(String email);
    List<LocalDate> getAvailableDates(Integer stationId);
}