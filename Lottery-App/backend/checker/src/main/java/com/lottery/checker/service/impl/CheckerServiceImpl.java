package com.lottery.checker.service.impl;

import com.lottery.checker.exception.BadRequestException;
import com.lottery.checker.exception.ConflictException;
import com.lottery.checker.exception.NotFoundException;

import com.lottery.checker.dto.request.CheckTicketsRequest;
import com.lottery.checker.dto.response.CheckTicketResponse;
import com.lottery.checker.dto.response.HistorySessionResponse;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CheckerServiceImpl implements CheckerService {

    private final LotteryResultRepository resultRepository;
    private final UserRepository userRepository;
    private final CheckSessionRepository sessionRepository;
    private final CheckHistoryRepository checkHistoryRepository;

    @Override
    @Transactional
    public CheckTicketResponse checkTickets(CheckTicketsRequest request, String userEmail) {
        validateRequest(request, userEmail);

        Integer stationId = request.stationId();
        LocalDate date = request.date();
        List<String> numbers = request.numbers();

        LotteryResult result = resultRepository
                .findByStationIdAndDrawDate(stationId, date)
                .filter(r -> "PUBLISH".equals(r.getStatus()))
                .orElseThrow(() -> new NotFoundException("No published results found for this date."));

        User user = userEmail != null ? userRepository.findByEmail(userEmail).orElse(null) : null;

        if (user != null) {
            List<String> cleanNumbers = numbers.stream().map(String::trim).toList();
            List<String> duplicates = checkHistoryRepository.findExistingTickets(user.getId(), result.getId(), cleanNumbers);
            if (!duplicates.isEmpty()) {
                throw new ConflictException("You have already checked ticket(s): " + String.join(", ", duplicates) + " for this draw. Each ticket can only be checked once.");
            }
        }

        CheckSession session = CheckSession.builder()
                .user(user)
                .totalSpent((long) numbers.size() * 10000)
                .build();

        long totalWon = 0;
        List<CheckTicketResponse.CheckDetail> detailResults = new ArrayList<>();

        for (String num : numbers) {
            CheckResult checkResult = processTicketCheck(session, result, num);
            totalWon += checkResult.amount();
            detailResults.add(checkResult.detail());
        }

        session.setTotalWon(totalWon);
        sessionRepository.save(session);
        resultRepository.incrementTotalQueries(result.getId());

        return new CheckTicketResponse(
                new CheckTicketResponse.CheckSummary(session.getTotalSpent(), totalWon),
                detailResults
        );
    }

    private void validateRequest(CheckTicketsRequest request, String userEmail) {
        if (request.date() != null && request.date().isAfter(LocalDate.now())) {
            throw new BadRequestException("Draw date cannot be in the future.");
        }

        if (userEmail == null && request.numbers().size() > 1) {
            throw new BadRequestException("Guests can only check one ticket at a time. Please login to check multiple tickets.");
        }

        Set<String> seen = new HashSet<>();
        for (String number : request.numbers()) {
            if (!seen.add(number.trim())) {
                throw new ConflictException("Duplicate ticket number in request: \"" + number.trim() + "\". Each ticket can only appear once.");
            }
        }
    }

    private record CheckResult(CheckTicketResponse.CheckDetail detail, long amount) {}

    private CheckResult processTicketCheck(CheckSession session, LotteryResult result, String rawNumber) {
        String cleanNum = rawNumber.trim();
        PrizeDetail winningPrize = findBestPrize(result.getPrizeDetails(), cleanNum);
        boolean isWon = winningPrize != null;
        long amount = isWon ? winningPrize.getRewardAmount() : 0;

        CheckHistory history = CheckHistory.builder()
                .session(session)
                .result(result)
                .ticketNumber(cleanNum)
                .isWon(isWon)
                .wonPrize(isWon ? winningPrize.getPrizeType() : null)
                .wonAmount(amount)
                .build();

        session.getHistories().add(history);

        CheckTicketResponse.CheckDetail detail = new CheckTicketResponse.CheckDetail(
                cleanNum,
                isWon,
                isWon ? winningPrize.getPrizeType() : "None",
                amount
        );

        return new CheckResult(detail, amount);
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
    public List<HistorySessionResponse> getUserHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<CheckSession> sessions = sessionRepository.findAllWithHistoriesByUserId(user.getId());

        return sessions.stream()
                .map(this::mapToHistorySessionResponse)
                .toList();
    }

    private HistorySessionResponse mapToHistorySessionResponse(CheckSession session) {
        List<HistorySessionResponse.HistoryTicketResponse> tickets = session.getHistories().stream()
                .map(this::mapToHistoryTicketResponse)
                .toList();

        return new HistorySessionResponse(
                session.getId(),
                session.getCreatedAt(),
                session.getTotalSpent(),
                session.getTotalWon(),
                tickets
        );
    }

    private HistorySessionResponse.HistoryTicketResponse mapToHistoryTicketResponse(CheckHistory history) {
        return new HistorySessionResponse.HistoryTicketResponse(
                history.getTicketNumber(),
                history.getIsWon(),
                history.getWonPrize() != null ? history.getWonPrize() : "No Prize",
                history.getWonAmount(),
                history.getResult().getStation().getName(),
                history.getResult().getDrawDate(),
                history.getCheckTime() != null ? history.getCheckTime() : history.getSession().getCreatedAt()
        );
    }
}