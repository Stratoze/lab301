package com.lottery.checker.service;

import com.lottery.checker.dto.request.CreateTicketRequest;
import com.lottery.checker.dto.request.PrizeRequest;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.TicketResponse;
import com.lottery.checker.entity.LotteryResult;
import com.lottery.checker.entity.LotteryStation;
import com.lottery.checker.entity.PrizeDetail;
import com.lottery.checker.repository.LotteryResultRepository;
import com.lottery.checker.repository.LotteryStationRepository;
import com.lottery.checker.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock private LotteryResultRepository resultRepository;
    @Mock private LotteryStationRepository stationRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private LotteryStation stationHCM;
    private LotteryResult savedResult;

    @BeforeEach
    void setUp() {
        stationHCM = LotteryStation.builder()
                .id(1)
                .stationCode("SOU-HCM")
                .name("TP. H? Chi Minh")
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

    // 2.1 Add ticket - valid input returns saved with UNPUBLISH
    @Test
    void addTicket_ValidInput_ReturnsSavedWithUnpublish() {
        when(stationRepository.findById(1)).thenReturn(Optional.of(stationHCM));
        when(resultRepository.findByResultCode("RES-HCM-23102023")).thenReturn(Optional.empty());
        when(resultRepository.save(any(LotteryResult.class))).thenAnswer(inv -> {
            LotteryResult r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        CreateTicketRequest request = new CreateTicketRequest(
                1,
                LocalDate.of(2023, 10, 23),
                List.of(
                        new PrizeRequest("G8", "85", 100000L),
                        new PrizeRequest("G_DB", "999999", 2000000000L)
                ),
                null // default UNPUBLISH
        );

        TicketResponse response = ticketService.createOrUpdateTicket(request);

        assertThat(response.status()).isEqualTo("UNPUBLISH");
        assertThat(response.resultCode()).isEqualTo("RES-HCM-23102023");
        assertThat(response.stationName()).isEqualTo("TP. H? Chi Minh");
        assertThat(response.prizes()).hasSize(2);
    }

    // 2.2 Search by station and date range returns filtered
    @Test
    void searchByStationAndDateRange_ReturnsFiltered() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LotteryResult> resultPage = new PageImpl<>(List.of(savedResult), pageable, 1);
        when(resultRepository.searchTickets(eq(1), eq(LocalDate.of(2023, 10, 1)),
                eq(LocalDate.of(2023, 10, 31)), isNull(), eq(pageable)))
                .thenReturn(resultPage);

        PagedResponse<TicketResponse> response = ticketService.searchTickets(
                1, LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 31), null, pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1L);
        assertThat(response.getContent().get(0).resultCode()).isEqualTo("RES-HCM-23102023");
    }

    // 2.3 Publish ticket - changes status to PUBLISH
    @Test
    void publishTicket_ChangesStatusToPublish() {
        when(resultRepository.findById(1L)).thenReturn(Optional.of(savedResult));
        when(resultRepository.save(any(LotteryResult.class))).thenAnswer(inv -> inv.getArgument(0));

        ticketService.updateStatus(1L, "PUBLISH");

        ArgumentCaptor<LotteryResult> resultCaptor = ArgumentCaptor.forClass(LotteryResult.class);
        verify(resultRepository).save(resultCaptor.capture());
        assertThat(resultCaptor.getValue().getStatus()).isEqualTo("PUBLISH");
    }

    // 2.4 Republishing an already-published ticket is idempotent (no-op)
    @Test
    void publishTicket_AlreadyPublished_DoesNotThrow() {
        // Current implementation allows re-setting status to PUBLISH silently.
        // TODO: Per SRS FR 3.13, the service should validate state transitions
        // and reject publishing an already-published ticket.
        savedResult.setStatus("PUBLISH");
        when(resultRepository.findById(1L)).thenReturn(Optional.of(savedResult));
        when(resultRepository.save(any(LotteryResult.class))).thenReturn(savedResult);

        ticketService.updateStatus(1L, "PUBLISH");

        verify(resultRepository).save(savedResult);
    }

    // 2.5 Same station+date updates existing result (upsert), preventing duplicates
    @Test
    void stationDateUniqueness_PreventsDuplicateViaUpsert() {
        // createOrUpdateTicket uses findByResultCode to find existing by resultCode,
        // which is derived from station+date. Same station+date = same resultCode = upsert.
        when(stationRepository.findById(1)).thenReturn(Optional.of(stationHCM));
        when(resultRepository.findByResultCode("RES-HCM-23102023")).thenReturn(Optional.of(savedResult));
        when(resultRepository.save(any(LotteryResult.class))).thenReturn(savedResult);

        CreateTicketRequest request = new CreateTicketRequest(
                1, LocalDate.of(2023, 10, 23),
                List.of(new PrizeRequest("G8", "12", 100000L)),
                null
        );

        TicketResponse response = ticketService.createOrUpdateTicket(request);

        // Returns the existing record ID (updated), not a new one
        assertThat(response.id()).isEqualTo(1L);
        verify(resultRepository, never()).save(argThat(r -> r.getId() == null));
        // TODO: Per SRS composite unique constraint (station_id, draw_date), consider
        // throwing a DataIntegrityViolationException or custom conflict error instead of
        // silently upserting, so the admin UI can show a clear duplicate-date message.
    }
}