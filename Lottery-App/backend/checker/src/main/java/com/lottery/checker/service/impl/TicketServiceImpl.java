package com.lottery.checker.service.impl;

import com.lottery.checker.dto.request.CreateTicketRequest;
import com.lottery.checker.dto.request.PrizeRequest;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.TicketResponse;
import com.lottery.checker.entity.LotteryResult;
import com.lottery.checker.entity.LotteryStation;
import com.lottery.checker.entity.PrizeDetail;
import com.lottery.checker.entity.User;
import com.lottery.checker.repository.LotteryResultRepository;
import com.lottery.checker.repository.LotteryStationRepository;
import com.lottery.checker.repository.UserRepository;
import com.lottery.checker.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final LotteryResultRepository resultRepository;
    private final LotteryStationRepository stationRepository;
    private final UserRepository userRepository;

    private static final Map<String, Integer> PRIZE_LENGTHS = Map.ofEntries(
            Map.entry("G_DB", 6),
            Map.entry("G1", 5),
            Map.entry("G2", 5),
            Map.entry("G3", 5),
            Map.entry("G4", 5),
            Map.entry("G5", 4),
            Map.entry("G6", 4),
            Map.entry("G7", 3),
            Map.entry("G8", 2)
    );

    @Override
    public List<LotteryStation> getAllStations() {
        return stationRepository.findAllByOrderByRegionAscNameAsc();
    }

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, String adminEmail) {
        LotteryStation station = stationRepository.findById(request.stationId())
                .orElseThrow(() -> new RuntimeException("Station not found"));

        String status = normalizeStatus(request.status());
        validatePrizes(request.prizes());

        Optional<LotteryResult> existing =
                resultRepository.findByStationIdAndDrawDate(request.stationId(), request.drawDate());

        if (existing.isPresent()) {
            throw new RuntimeException("A result already exists for this station and draw date.");
        }

        LotteryResult result = LotteryResult.builder()
                .resultCode(generateResultCode(station, request.drawDate()))
                .station(station)
                .drawDate(request.drawDate())
                .status(status)
                .totalQueries(0L)
                .build();

        applyAuditAndPrizes(result, request.prizes(), status, adminEmail);

        return TicketResponse.fromEntity(resultRepository.save(result));
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(Long id, CreateTicketRequest request, String adminEmail) {
        LotteryResult result = resultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        LotteryStation station = stationRepository.findById(request.stationId())
                .orElseThrow(() -> new RuntimeException("Station not found"));

        String status = normalizeStatus(request.status());
        validatePrizes(request.prizes());

        Optional<LotteryResult> existing =
                resultRepository.findByStationIdAndDrawDate(request.stationId(), request.drawDate());

        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new RuntimeException("A result already exists for this station and draw date.");
        }

        result.setStation(station);
        result.setDrawDate(request.drawDate());
        result.setResultCode(generateResultCode(station, request.drawDate()));

        applyAuditAndPrizes(result, request.prizes(), status, adminEmail);

        return TicketResponse.fromEntity(resultRepository.save(result));
    }

    @Override
    public PagedResponse<TicketResponse> searchTickets(
            Integer stationId,
            LocalDate start,
            LocalDate end,
            String keyword,
            Pageable pageable
    ) {
        Page<LotteryResult> page = resultRepository.searchTickets(stationId, start, end, keyword, pageable);

        List<TicketResponse> content = page.getContent().stream()
                .map(TicketResponse::fromEntity)
                .toList();

        return PagedResponse.<TicketResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        updateStatus(id, status, null);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status, String adminEmail) {
        LotteryResult result = resultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        String normalizedStatus = normalizeStatus(status);
        result.setStatus(normalizedStatus);

        if ("PUBLISH".equals(normalizedStatus)) {
            User admin = resolveAdmin(adminEmail);
            result.setPublishedBy(admin);
            result.setPublishedAt(LocalDateTime.now());
        }

        resultRepository.save(result);
    }

    private void applyAuditAndPrizes(
            LotteryResult result,
            List<PrizeRequest> prizes,
            String status,
            String adminEmail
    ) {
        User admin = resolveAdmin(adminEmail);

        if (result.getCreatedBy() == null) {
            result.setCreatedBy(admin);
        }

        if ("PUBLISH".equals(status)) {
            result.setPublishedBy(admin);
            result.setPublishedAt(LocalDateTime.now());
        }

        result.getPrizeDetails().clear();

        for (PrizeRequest prizeReq : prizes) {
            String[] numbers = prizeReq.winningNumbers().split("[,\\s]+");

            for (String num : numbers) {
                String cleaned = num.trim();

                if (!cleaned.isBlank()) {
                    result.addPrizeDetail(
                            PrizeDetail.builder()
                                    .prizeType(prizeReq.type().toUpperCase())
                                    .winningNumber(cleaned)
                                    .rewardAmount(prizeReq.rewardAmount())
                                    .build()
                    );
                }
            }
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "UNPUBLISH";
        }

        String normalized = status.trim().toUpperCase();

        if (!normalized.equals("PUBLISH") && !normalized.equals("UNPUBLISH")) {
            throw new RuntimeException("Invalid ticket status. Allowed values are PUBLISH or UNPUBLISH.");
        }

        return normalized;
    }

    private void validatePrizes(List<PrizeRequest> prizes) {
        if (prizes == null || prizes.isEmpty()) {
            throw new RuntimeException("Prize details are required");
        }

        Set<String> seen = new HashSet<>();

        for (PrizeRequest prize : prizes) {
            if (prize.type() == null || prize.type().isBlank()) {
                throw new RuntimeException("Prize type is required");
            }

            if (prize.rewardAmount() == null) {
                throw new RuntimeException("Reward amount is required");
            }

            String type = prize.type().trim().toUpperCase();
            Integer expectedLength = PRIZE_LENGTHS.get(type);

            if (expectedLength == null) {
                throw new RuntimeException("Invalid prize type: " + type);
            }

            if (prize.winningNumbers() == null || prize.winningNumbers().isBlank()) {
                throw new RuntimeException("Winning numbers are required for prize " + type);
            }

            String[] numbers = prize.winningNumbers().split("[,\\s]+");

            for (String rawNumber : numbers) {
                String number = rawNumber.trim();

                if (number.isBlank()) {
                    continue;
                }

                if (!number.matches("\\d+")) {
                    throw new RuntimeException("Winning numbers must contain digits only. Invalid value: " + number);
                }

                if (number.length() != expectedLength) {
                    throw new RuntimeException(
                            "Prize " + type + " requires " + expectedLength +
                            "-digit numbers. Invalid value: " + number
                    );
                }

                String key = type + ":" + number;

                if (!seen.add(key)) {
                    throw new RuntimeException("Duplicate winning number detected for prize " + type + ": " + number);
                }
            }
        }
    }

    private String generateResultCode(LotteryStation station, LocalDate drawDate) {
        String dateStr = drawDate.format(DateTimeFormatter.ofPattern("ddMMyyyy"));

        String cityCode = station.getStationCode().contains("-")
                ? station.getStationCode().substring(station.getStationCode().lastIndexOf('-') + 1)
                : station.getStationCode();

        return String.format("RES-%s-%s", cityCode, dateStr);
    }

    private User resolveAdmin(String adminEmail) {
        if (adminEmail == null || adminEmail.isBlank()) {
            return null;
        }

        return userRepository.findByEmail(adminEmail).orElse(null);
    }
}