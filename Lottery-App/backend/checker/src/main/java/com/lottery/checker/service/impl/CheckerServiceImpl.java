package com.lottery.checker.service.impl;

import com.lottery.checker.entity.CheckHistory;
import com.lottery.checker.entity.CheckSession;
import com.lottery.checker.entity.LotteryResult;
import com.lottery.checker.entity.PrizeDetail;
import com.lottery.checker.entity.User;
import com.lottery.checker.repository.CheckHistoryRepository;
import com.lottery.checker.repository.CheckSessionRepository;
import com.lottery.checker.repository.LotteryResultRepository;
import com.lottery.checker.repository.UserRepository;
import com.lottery.checker.service.CheckerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckerServiceImpl implements CheckerService {

    private final LotteryResultRepository resultRepository;
    private final UserRepository userRepository;
    private final CheckSessionRepository sessionRepository;
    private final CheckHistoryRepository checkHistoryRepository;

    @Override
    @Transactional
    public Map<String, Object> checkTickets(
            Integer stationId,
            LocalDate date,
            List<String> numbers,
            String userEmail
    ) {
        LotteryResult result = resultRepository
                .findByStationIdAndDrawDate(stationId, date)
                .filter(r -> "PUBLISH".equals(r.getStatus()))
                .orElseThrow(() -> new RuntimeException("No published results found for this date."));

        User user = userEmail != null
                ? userRepository.findByEmail(userEmail).orElse(null)
                : null;

        if (user != null) {
            // Batch check for duplicates in a single query (avoids N+1)
            List<String> cleanNumbers = numbers.stream().map(String::trim).toList();
            List<String> duplicates = checkHistoryRepository.findExistingTickets(
                    user.getId(),
                    result.getId(),
                    cleanNumbers
            );

            if (!duplicates.isEmpty()) {
                throw new RuntimeException(
                        "You have already checked ticket(s): " +
                        String.join(", ", duplicates) +
                        " for this draw. Each ticket can only be checked once."
                );
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

        // Atomic increment to avoid race condition under concurrent requests
        resultRepository.incrementTotalQueries(result.getId());

        return Map.of(
                "summary", Map.of(
                        "totalSpent", session.getTotalSpent(),
                        "totalWon", totalWon
                ),
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
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CheckSession> sessions =
                sessionRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());

        return sessions.stream()
                .map(session -> {
                    Map<String, Object> map = new HashMap<>();

                    map.put("id", session.getId());
                    map.put("date", session.getCreatedAt());
                    map.put("totalSpent", session.getTotalSpent());
                    map.put("totalWon", session.getTotalWon());

                    List<Map<String, Object>> tickets = session.getHistories().stream()
                            .map(history -> {
                                Map<String, Object> ticket = new HashMap<>();

                                ticket.put("number", history.getTicketNumber());
                                ticket.put("isWon", history.getIsWon());
                                ticket.put(
                                        "prize",
                                        history.getWonPrize() != null
                                                ? history.getWonPrize()
                                                : "No Prize"
                                );
                                ticket.put("amount", history.getWonAmount());
                                ticket.put("station", history.getResult().getStation().getName());
                                ticket.put("drawDate", history.getResult().getDrawDate());
                                ticket.put(
                                        "checkTime",
                                        history.getCheckTime() != null
                                                ? history.getCheckTime()
                                                : session.getCreatedAt()
                                );

                                return ticket;
                            })
                            .toList();

                    map.put("tickets", tickets);

                    return map;
                })
                .toList();
    }
}