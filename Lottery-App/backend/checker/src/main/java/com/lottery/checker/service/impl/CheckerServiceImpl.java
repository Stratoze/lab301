package com.lottery.checker.service.impl;

import com.lottery.checker.exception.ConflictException;
import com.lottery.checker.exception.NotFoundException;

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
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckerServiceImpl implements CheckerService {

    private final LotteryResultRepository resultRepository;
    private final UserRepository userRepository;
    private final CheckSessionRepository sessionRepository;
    private final CheckHistoryRepository checkHistoryRepository;

    @Override
    @Transactional
    public com.lottery.checker.dto.response.CheckTicketResponse checkTickets(
            com.lottery.checker.dto.request.CheckTicketsRequest request,
            String userEmail
    ) {
        Integer stationId = request.stationId();
        LocalDate date = request.date();
        List<String> numbers = request.numbers();

        LotteryResult result = resultRepository
                .findByStationIdAndDrawDate(stationId, date)
                .filter(r -> "PUBLISH".equals(r.getStatus()))
                .orElseThrow(() -> new NotFoundException("No published results found for this date."));

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
                throw new ConflictException("You have already checked ticket(s): " + String.join(", ", duplicates) + " for this draw. Each ticket can only be checked once.");
            }
        }

        CheckSession session = CheckSession.builder()
                .user(user)
                .totalSpent((long) numbers.size() * 10000)
                .build();

        long totalWon = 0;
        List<com.lottery.checker.dto.response.CheckTicketResponse.CheckDetail> detailResults = new ArrayList<>();

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

            detailResults.add(new com.lottery.checker.dto.response.CheckTicketResponse.CheckDetail(
                    cleanNum,
                    isWon,
                    isWon ? winningPrize.getPrizeType() : "None",
                    amount
            ));
        }

        session.setTotalWon(totalWon);
        sessionRepository.save(session);

        // Atomic increment to avoid race condition under concurrent requests
        resultRepository.incrementTotalQueries(result.getId());

        return new com.lottery.checker.dto.response.CheckTicketResponse(
                new com.lottery.checker.dto.response.CheckTicketResponse.CheckSummary(session.getTotalSpent(), totalWon),
                detailResults
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
    public List<com.lottery.checker.dto.response.HistorySessionResponse> getUserHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<CheckSession> sessions =
                sessionRepository.findAllWithHistoriesByUserId(user.getId());

        return sessions.stream()
                .map(session -> {
                    List<com.lottery.checker.dto.response.HistorySessionResponse.HistoryTicketResponse> tickets = session.getHistories().stream()
                            .map(history -> new com.lottery.checker.dto.response.HistorySessionResponse.HistoryTicketResponse(
                                    history.getTicketNumber(),
                                    history.getIsWon(),
                                    history.getWonPrize() != null ? history.getWonPrize() : "No Prize",
                                    history.getWonAmount(),
                                    history.getResult().getStation().getName(),
                                    history.getResult().getDrawDate(),
                                    history.getCheckTime() != null ? history.getCheckTime() : session.getCreatedAt()
                            ))
                            .toList();

                    return new com.lottery.checker.dto.response.HistorySessionResponse(
                            session.getId(),
                            session.getCreatedAt(),
                            session.getTotalSpent(),
                            session.getTotalWon(),
                            tickets
                    );
                })
                .toList();
    }
}