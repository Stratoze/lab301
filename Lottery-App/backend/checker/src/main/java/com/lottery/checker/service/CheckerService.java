package com.lottery.checker.service;

import com.lottery.checker.dto.request.CheckTicketsRequest;
import com.lottery.checker.dto.response.CheckTicketResponse;
import com.lottery.checker.dto.response.HistorySessionResponse;
import java.time.LocalDate;
import java.util.List;

public interface CheckerService {

    CheckTicketResponse checkTickets(CheckTicketsRequest request, String userEmail);

    List<HistorySessionResponse> getUserHistory(String email);

    List<LocalDate> getAvailableDates(Integer stationId);
}