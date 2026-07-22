package com.lottery.checker.service;

import com.lottery.checker.dto.request.CreateTicketRequest;
import com.lottery.checker.dto.request.PrizeRequest;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.TicketResponse;
import com.lottery.checker.entity.LotteryResult;
import com.lottery.checker.entity.LotteryStation;
import com.lottery.checker.repository.LotteryResultRepository;
import com.lottery.checker.repository.LotteryStationRepository;
import com.lottery.checker.repository.UserRepository;
import com.lottery.checker.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private LotteryResultRepository resultRepository;

    @Mock
    private LotteryStationRepository stationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private LotteryStation stationHCM;
    private LotteryResult savedResult;

    @BeforeEach
    void setUp() {
        stationHCM = LotteryStation.builder()
                .id(1)
                .stationCode("SOU-HCM")
                .name("TP. Ho Chi Minh")
                .region("SOUTH")
                .build();

        savedResult = LotteryResult.builder()
                .id(1L)
                .resultCode("RES-HCM-23102023")
                .station(stationHCM)
                .drawDate(LocalDate.of(2023, 10, 23))
                .status("UNPUBLISH")
                .totalQueries(0L)
                .prizeDetails(new ArrayList<>())
                .build();
    }

    @Test
    void addTicket_ValidInput_ReturnsSavedWithUnpublish() {
        LocalDate drawDate = LocalDate.of(2023, 10, 23);

        when(stationRepository.findById(1)).thenReturn(Optional.of(stationHCM));
        when(resultRepository.findByStationIdAndDrawDate(1, drawDate))
                .thenReturn(Optional.empty());
        when(resultRepository.save(any(LotteryResult.class))).thenAnswer(inv -> {
            LotteryResult r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        CreateTicketRequest request = new CreateTicketRequest(
                1,
                drawDate,
                List.of(
                        new PrizeRequest("G8", "85", 100000L),
                        new PrizeRequest("G_DB", "999999", 2000000000L)
                ),
                null
        );

        TicketResponse response = ticketService.createOrUpdateTicket(request);

        assertThat(response.status()).isEqualTo("UNPUBLISH");
        assertThat(response.resultCode()).isEqualTo("RES-HCM-23102023");
        assertThat(response.stationName()).isEqualTo("TP. Ho Chi Minh");
        assertThat(response.prizes()).hasSize(2);
    }

    @Test
    void addTicket_DuplicateStationAndDate_ThrowsConflict() {
        LocalDate drawDate = LocalDate.of(2023, 10, 23);

        when(stationRepository.findById(1)).thenReturn(Optional.of(stationHCM));
        when(resultRepository.findByStationIdAndDrawDate(1, drawDate))
                .thenReturn(Optional.of(savedResult));

        CreateTicketRequest request = new CreateTicketRequest(
                1,
                drawDate,
                List.of(
                        new PrizeRequest("G8", "85", 100000L),
                        new PrizeRequest("G_DB", "999999", 2000000000L)
                ),
                null
        );

        assertThatThrownBy(() -> ticketService.createOrUpdateTicket(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");

        verify(resultRepository, never()).save(any());
    }

    @Test
    void addTicket_InvalidPrizeLength_ThrowsError() {
        LocalDate drawDate = LocalDate.of(2023, 10, 23);

        when(stationRepository.findById(1)).thenReturn(Optional.of(stationHCM));

        CreateTicketRequest request = new CreateTicketRequest(
                1,
                drawDate,
                List.of(
                        new PrizeRequest("G8", "123", 100000L)
                ),
                null
        );

        assertThatThrownBy(() -> ticketService.createOrUpdateTicket(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("2-digit numbers");

        verify(resultRepository, never()).save(any());
    }

    @Test
    void addTicket_NonNumericPrize_ThrowsError() {
        LocalDate drawDate = LocalDate.of(2023, 10, 23);

        when(stationRepository.findById(1)).thenReturn(Optional.of(stationHCM));

        CreateTicketRequest request = new CreateTicketRequest(
                1,
                drawDate,
                List.of(
                        new PrizeRequest("G8", "AB", 100000L)
                ),
                null
        );

        assertThatThrownBy(() -> ticketService.createOrUpdateTicket(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("digits only");

        verify(resultRepository, never()).save(any());
    }

    @Test
    void searchByStationAndDateRange_ReturnsFiltered() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LotteryResult> resultPage = new PageImpl<>(List.of(savedResult), pageable, 1);

        when(resultRepository.searchTickets(
                eq(1),
                eq(LocalDate.of(2023, 10, 1)),
                eq(LocalDate.of(2023, 10, 31)),
                isNull(),
                eq(pageable)
        )).thenReturn(resultPage);

        PagedResponse<TicketResponse> response = ticketService.searchTickets(
                1,
                LocalDate.of(2023, 10, 1),
                LocalDate.of(2023, 10, 31),
                null,
                pageable
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1L);
        assertThat(response.getContent().get(0).resultCode()).isEqualTo("RES-HCM-23102023");
    }

    @Test
    void publishTicket_ChangesStatusToPublish() {
        when(resultRepository.findById(1L)).thenReturn(Optional.of(savedResult));
        when(resultRepository.save(any(LotteryResult.class))).thenAnswer(inv -> inv.getArgument(0));

        ticketService.updateStatus(1L, "PUBLISH");

        ArgumentCaptor<LotteryResult> resultCaptor = ArgumentCaptor.forClass(LotteryResult.class);
        verify(resultRepository).save(resultCaptor.capture());

        assertThat(resultCaptor.getValue().getStatus()).isEqualTo("PUBLISH");
        assertThat(resultCaptor.getValue().getPublishedAt()).isNotNull();
    }

    @Test
    void unpublishTicket_ChangesStatusToUnpublish() {
        savedResult.setStatus("PUBLISH");

        when(resultRepository.findById(1L)).thenReturn(Optional.of(savedResult));
        when(resultRepository.save(any(LotteryResult.class))).thenAnswer(inv -> inv.getArgument(0));

        ticketService.updateStatus(1L, "UNPUBLISH");

        ArgumentCaptor<LotteryResult> resultCaptor = ArgumentCaptor.forClass(LotteryResult.class);
        verify(resultRepository).save(resultCaptor.capture());

        assertThat(resultCaptor.getValue().getStatus()).isEqualTo("UNPUBLISH");
    }
}