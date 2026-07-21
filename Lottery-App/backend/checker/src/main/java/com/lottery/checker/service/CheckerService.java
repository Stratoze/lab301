package com.lottery.checker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CheckerService {
    Map<String, Object> checkTickets(Integer stationId, LocalDate date, List<String> numbers, String userEmail);
    List<Map<String, Object>> getUserHistory(String email);
    List<LocalDate> getAvailableDates(Integer stationId);
}