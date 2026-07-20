package com.lottery.checker.service.impl;

import com.lottery.checker.entity.*;
import com.lottery.checker.repository.*;
import com.lottery.checker.service.CheckerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CheckerServiceImpl implements CheckerService {

    private final LotteryResultRepository resultRepository;
    private final UserRepository userRepository;
    private final CheckSessionRepository sessionRepository;
    private final CheckHistoryRepository checkHistoryRepository;

    @Override
    @Transactional
    public Map<String, Object> checkTickets(Integer stationId, LocalDate date, List<String> numbers, String userEmail) {
        LotteryResult result = resultRepository.searchTickets(stationId, date, date, null, null)
                .getContent().stream()
                .filter(r -> r.getStatus().equals("PUBLISH"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No published results found for this date."));

        User user = (userEmail != null) ? userRepository.findByEmail(userEmail).orElse(null) : null;
        
        // --- DUPLICATE CHECK for logged-in users ---
        if (user != null) {
            List<String> duplicates = new ArrayList<>();
            for (String num : numbers) {
                String clean = num.trim();
                if (checkHistoryRepository.existsByUserAndResultAndTicket(user.getId(), result.getId(), clean)) {
                    duplicates.add(clean);
                }
            }
            if (!duplicates.isEmpty()) {
                throw new RuntimeException("You have already checked ticket(s): " + String.join(", ", duplicates) + 
                    " for this draw. Each ticket can only be checked once.");
            }
        }

        CheckSession session = CheckSession.builder()
                .user(user)
                .totalSpent((long) numbers.size() * 10000)
                .build();

        long totalWon = 0;
        List<Map<String, Object>> detailResults = new ArrayList<>();

        for (String num : numbers) {
            String cleanNum = num.trim();
            PrizeDetail winningPrize = findBestPrize(result.getPrizeDetails(), cleanNum);
            
            boolean isWon = winningPrize != null;
            long amount = isWon ? winningPrize.getRewardAmount() : 0;
            totalWon += amount;

            CheckHistory history = CheckHistory.builder()
                    .session(session)
                    .result(result)
                    .ticketNumber(cleanNum)
                    .isWon(isWon)
                    .wonPrize(isWon ? winningPrize.getPrizeType() : null)
                    .wonAmount(amount)
                    .build();
            
            session.getHistories().add(history);
            
            detailResults.add(Map.of(
                "number", cleanNum,
                "isWon", isWon,
                "prize", isWon ? winningPrize.getPrizeType() : "None",
                "amount", amount
            ));
        }

        session.setTotalWon(totalWon);
        sessionRepository.save(session);

        // Update total queries for ticket
        result.setTotalQueries(result.getTotalQueries() + 1);
        resultRepository.save(result);

        return Map.of(
            "summary", Map.of("totalSpent", session.getTotalSpent(), "totalWon", totalWon),
            "details", detailResults
        );
    }

    private PrizeDetail findBestPrize(List<PrizeDetail> prizes, String ticketNum) {
        return prizes.stream()
                .filter(p -> ticketNum.endsWith(p.getWinningNumber()))
                .max(Comparator.comparingLong(PrizeDetail::getRewardAmount))
                .orElse(null);
    }

    @Override
    public List<LocalDate> getAvailableDates(Integer stationId) {
        return resultRepository.findDrawDatesByStation(stationId);
    }

    @Override
    public List<Map<String, Object>> getUserHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<CheckSession> sessions = sessionRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
        
        return sessions.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("date", s.getCreatedAt());
            map.put("totalSpent", s.getTotalSpent());
            map.put("totalWon", s.getTotalWon());
            map.put("tickets", s.getHistories().stream().map(h -> Map.of(
                "number", h.getTicketNumber(),
                "isWon", h.getIsWon(),
                "prize", h.getWonPrize() != null ? h.getWonPrize() : "No Prize",
                "amount", h.getWonAmount(),
                "station", h.getResult().getStation().getName()
            )).toList());
            return map;
        }).toList();
    }
}