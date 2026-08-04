package com.lottery.checker.service.impl;

import com.lottery.checker.exception.BadRequestException;
import com.lottery.checker.exception.ConflictException;
import com.lottery.checker.exception.NotFoundException;

import com.lottery.checker.dto.request.CreateTicketRequest;
import com.lottery.checker.dto.request.PrizeRequest;
import com.lottery.checker.dto.request.UpdateTicketStatusRequest;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
                .orElseThrow(() -> new NotFoundException("Station not found"));

        String status = normalizeStatus(request.status());
        validatePrizes(request.prizes());

        Optional<LotteryResult> existing =
                resultRepository.findByStationIdAndDrawDate(request.stationId(), request.drawDate());

        if (existing.isPresent()) {
            throw new ConflictException("A result already exists for this station and draw date.");
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
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        LotteryStation station = stationRepository.findById(request.stationId())
                .orElseThrow(() -> new NotFoundException("Station not found"));

        String status = normalizeStatus(request.status());
        validatePrizes(request.prizes());

        Optional<LotteryResult> existing =
                resultRepository.findByStationIdAndDrawDate(request.stationId(), request.drawDate());

        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new ConflictException("A result already exists for this station and draw date.");
        }

        result.setStation(station);
        result.setDrawDate(request.drawDate());
        result.setResultCode(generateResultCode(station, request.drawDate()));
        result.setStatus(status);

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
    public void updateStatus(Long id, UpdateTicketStatusRequest request, String adminEmail) {
        LotteryResult result = resultRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        String normalizedStatus = normalizeStatus(request.status());
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

        // Flatten the incoming request into an ordered type:number -> amount map.
        LinkedHashMap<String, Long> wanted = new LinkedHashMap<>();
        for (PrizeRequest prizeReq : prizes) {
            String type = prizeReq.type().trim().toUpperCase();
            for (String num : prizeReq.winningNumbers().split("[,\\s]+")) {
                String cleaned = num.trim();
                if (!cleaned.isBlank()) {
                    wanted.put(type + ":" + cleaned, prizeReq.rewardAmount());
                }
            }
        }

        // Diff against the current collection instead of clear()+re-add.
        // Hibernate flushes INSERTs before DELETEs, so clearing then re-adding an
        // unchanged (result_id, prize_type, winning_number) row violates the
        // unique_idx_result_prize_number constraint on edit (the 409 "Duplicate
        // or invalid data" bug). Reusing the managed entity issues a clean UPDATE;
        // only rows that truly vanish are orphan-removed.
        Map<String, PrizeDetail> currentByKey = new HashMap<>();
        for (PrizeDetail detail : result.getPrizeDetails()) {
            currentByKey.put(detail.getPrizeType() + ":" + detail.getWinningNumber(), detail);
        }

        Set<String> wantedKeys = new HashSet<>(wanted.keySet());
        result.getPrizeDetails().removeIf(detail ->
                !wantedKeys.contains(detail.getPrizeType() + ":" + detail.getWinningNumber()));

        for (Map.Entry<String, Long> entry : wanted.entrySet()) {
            PrizeDetail existing = currentByKey.get(entry.getKey());
            if (existing != null) {
                existing.setRewardAmount(entry.getValue());
            } else {
                int sep = entry.getKey().indexOf(':');
                result.addPrizeDetail(
                        PrizeDetail.builder()
                                .prizeType(entry.getKey().substring(0, sep))
                                .winningNumber(entry.getKey().substring(sep + 1))
                                .rewardAmount(entry.getValue())
                                .build()
                );
            }
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "UNPUBLISH";
        }

        String normalized = status.trim().toUpperCase();

        if (!normalized.equals("PUBLISH") && !normalized.equals("UNPUBLISH")) {
            throw new BadRequestException("Invalid ticket status. Allowed values are PUBLISH or UNPUBLISH.");
        }

        return normalized;
    }

    private void validatePrizes(List<PrizeRequest> prizes) {
        if (prizes == null || prizes.isEmpty()) {
            throw new BadRequestException("Prize details are required");
        }

        Set<String> seen = new HashSet<>();

        for (PrizeRequest prize : prizes) {
            if (prize.type() == null || prize.type().isBlank()) {
                throw new BadRequestException("Prize type is required");
            }

            if (prize.rewardAmount() == null) {
                throw new BadRequestException("Reward amount is required");
            }

            String type = prize.type().trim().toUpperCase();
            Integer expectedLength = PRIZE_LENGTHS.get(type);

            if (expectedLength == null) {
                throw new BadRequestException("Invalid prize type: " + type);
            }

            if (prize.winningNumbers() == null || prize.winningNumbers().isBlank()) {
                throw new BadRequestException("Winning numbers are required for prize " + type);
            }

            String[] numbers = prize.winningNumbers().split("[,\\s]+");

            for (String rawNumber : numbers) {
                String number = rawNumber.trim();

                if (number.isBlank()) {
                    continue;
                }

                if (!number.matches("\\d+")) {
                    throw new BadRequestException("Winning numbers must contain digits only. Invalid value: " + number);
                }

                if (number.length() != expectedLength) {
                    throw new BadRequestException("Prize " + type + " requires " + expectedLength + "-digit numbers. Invalid value: " + number);
                }

                String key = type + ":" + number;

                if (!seen.add(key)) {
                    throw new ConflictException("Duplicate winning number detected for prize " + type + ": " + number);
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