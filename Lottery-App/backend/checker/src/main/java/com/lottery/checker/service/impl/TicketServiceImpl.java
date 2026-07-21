package com.lottery.checker.service.impl;

import com.lottery.checker.dto.request.CreateTicketRequest;
import com.lottery.checker.dto.request.PrizeRequest;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.TicketResponse;
import com.lottery.checker.entity.LotteryResult;
import com.lottery.checker.entity.LotteryStation;
import com.lottery.checker.entity.PrizeDetail;
import com.lottery.checker.repository.LotteryResultRepository;
import com.lottery.checker.repository.LotteryStationRepository;
import com.lottery.checker.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final LotteryResultRepository resultRepository;
    private final LotteryStationRepository stationRepository;

    @Override
    public List<LotteryStation> getAllStations() {
        return stationRepository.findAllByOrderByRegionAscNameAsc();
    }

    @Override
    @Transactional
    public TicketResponse createOrUpdateTicket(CreateTicketRequest request) {
        LotteryStation station = stationRepository.findById(request.stationId())
                .orElseThrow(() -> new RuntimeException("Station not found"));

        // Generate Result Code: RES-XXX-DDMMYYYY (XXX = city abbreviation)
        String dateStr = request.drawDate().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        String cityCode = station.getStationCode().contains("-")
                ? station.getStationCode().substring(station.getStationCode().lastIndexOf('-') + 1)
                : station.getStationCode();
        String resultCode = String.format("RES-%s-%s", cityCode, dateStr);

        // Check if exists to update or create new
        LotteryResult result = resultRepository.findByResultCode(resultCode)
                .orElse(LotteryResult.builder()
                        .resultCode(resultCode)
                        .station(station)
                        .drawDate(request.drawDate())
                        .build());

        result.setStatus(request.status() != null ? request.status() : "UNPUBLISH");
        
        // Clear existing prize details for clean update
        result.getPrizeDetails().clear();

        // Parse and add prize details
        for (PrizeRequest prizeReq : request.prizes()) {
            // Split by comma, space, or newline
            String[] numbers = prizeReq.winningNumbers().split("[,\\s\\n]+");
            for (String num : numbers) {
                if (!num.isBlank()) {
                    result.addPrizeDetail(PrizeDetail.builder()
                            .prizeType(prizeReq.type())
                            .winningNumber(num.trim())
                            .rewardAmount(prizeReq.rewardAmount())
                            .build());
                }
            }
        }

        return TicketResponse.fromEntity(resultRepository.save(result));
    }

    @Override
    public PagedResponse<TicketResponse> searchTickets(Integer stationId, LocalDate start, LocalDate end, String keyword, Pageable pageable) {
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
        LotteryResult result = resultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        result.setStatus(status);
        resultRepository.save(result);
    }
}