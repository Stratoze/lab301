package com.lottery.checker.service;

import com.lottery.checker.dto.request.CheckTicketsRequest;
import com.lottery.checker.dto.response.CheckTicketResponse;
import com.lottery.checker.dto.response.HistorySessionResponse;
import com.lottery.checker.entity.CheckHistory;
import com.lottery.checker.entity.CheckSession;
import com.lottery.checker.entity.LotteryResult;
import com.lottery.checker.entity.LotteryStation;
import com.lottery.checker.entity.PrizeDetail;
import com.lottery.checker.entity.Role;
import com.lottery.checker.entity.User;
import com.lottery.checker.repository.CheckHistoryRepository;
import com.lottery.checker.repository.CheckSessionRepository;
import com.lottery.checker.repository.LotteryResultRepository;
import com.lottery.checker.repository.UserRepository;
import com.lottery.checker.service.impl.CheckerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckerServiceImplTest {

    @Mock
    private LotteryResultRepository resultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CheckSessionRepository sessionRepository;

    @Mock
    private CheckHistoryRepository checkHistoryRepository;

    @InjectMocks
    private CheckerServiceImpl checkerService;

    private LotteryResult publishedResult;
    private LotteryStation station;
    private User testUser;

    private final LocalDate drawDate = LocalDate.of(2023, 10, 23);

    @BeforeEach
    void setUp() {
        station = LotteryStation.builder()
                .id(1)
                .stationCode("SOU-HCM")
                .name("TP. Ho Chi Minh")
                .region("SOUTH")
                .build();

        publishedResult = LotteryResult.builder()
                .id(1L)
                .resultCode("RES-HCM-23102023")
                .station(station)
                .drawDate(drawDate)
                .status("PUBLISH")
                .totalQueries(0L)
                .prizeDetails(new ArrayList<>())
                .build();

        publishedResult.addPrizeDetail(
                PrizeDetail.builder()
                        .prizeType("G8")
                        .winningNumber("85")
                        .rewardAmount(100000L)
                        .build()
        );

        publishedResult.addPrizeDetail(
                PrizeDetail.builder()
                        .prizeType("G7")
                        .winningNumber("763")
                        .rewardAmount(200000L)
                        .build()
        );

        publishedResult.addPrizeDetail(
                PrizeDetail.builder()
                        .prizeType("G_DB")
                        .winningNumber("999999")
                        .rewardAmount(2000000000L)
                        .build()
        );

        testUser = User.builder()
                .id(3L)
                .userCode("USR-10-2023-00000003")
                .email("khach1@gmail.com")
                .fullName("Le Van Tam")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();
    }

    private void mockPublishedResultLookup() {
        when(resultRepository.findByStationIdAndDrawDate(1, drawDate))
                .thenReturn(Optional.of(publishedResult));
    }

    @Test
    void guestCheck_WinningTicket_ReturnsCorrectPrize() {
        mockPublishedResultLookup();

        CheckTicketsRequest req = new CheckTicketsRequest(1, drawDate, List.of("123485"));
        CheckTicketResponse response = checkerService.checkTickets(req, null);

        ArgumentCaptor<CheckSession> sessionCaptor =
                ArgumentCaptor.forClass(CheckSession.class);

        verify(sessionRepository).save(sessionCaptor.capture());

        CheckSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getUser()).isNull();
        assertThat(savedSession.getTotalSpent()).isEqualTo(10000L);
        assertThat(savedSession.getTotalWon()).isEqualTo(100000L);

        assertThat(response.details()).hasSize(1);
        assertThat(response.details().get(0).isWon()).isTrue();
        assertThat(response.details().get(0).prize()).isEqualTo("G8");
        assertThat(response.details().get(0).amount()).isEqualTo(100000L);
    }

    @Test
    void guestCheck_LosingTicket_ReturnsNoPrize() {
        mockPublishedResultLookup();

        CheckTicketsRequest req = new CheckTicketsRequest(1, drawDate, List.of("000000"));
        CheckTicketResponse response = checkerService.checkTickets(req, null);

        ArgumentCaptor<CheckSession> sessionCaptor =
                ArgumentCaptor.forClass(CheckSession.class);

        verify(sessionRepository).save(sessionCaptor.capture());

        CheckSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getTotalSpent()).isEqualTo(10000L);
        assertThat(savedSession.getTotalWon()).isEqualTo(0L);

        assertThat(response.details().get(0).isWon()).isFalse();
        assertThat(response.details().get(0).prize()).isEqualTo("None");
        assertThat(response.details().get(0).amount()).isEqualTo(0L);
    }

    @Test
    void bulkCheck_MultiTickets_EvaluatesAll() {
        mockPublishedResultLookup();

        when(userRepository.findByEmail("khach1@gmail.com"))
                .thenReturn(Optional.of(testUser));

        when(checkHistoryRepository.findExistingTickets(
                anyLong(),
                anyLong(),
                anyList()
        )).thenReturn(List.of());

        CheckTicketsRequest req = new CheckTicketsRequest(1, drawDate, List.of("123485", "000000", "999999"));
        CheckTicketResponse response = checkerService.checkTickets(req, "khach1@gmail.com");

        ArgumentCaptor<CheckSession> sessionCaptor =
                ArgumentCaptor.forClass(CheckSession.class);

        verify(sessionRepository).save(sessionCaptor.capture());

        CheckSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getTotalSpent()).isEqualTo(30000L);
        assertThat(savedSession.getTotalWon()).isEqualTo(100000L + 2000000000L);
        assertThat(savedSession.getHistories()).hasSize(3);
        assertThat(response.details()).hasSize(3);
    }

    @Test
    void bestPrizeSelection_MultipleMatches_TakesHighest() {
        publishedResult.addPrizeDetail(
                PrizeDetail.builder()
                        .prizeType("G6")
                        .winningNumber("85")
                        .rewardAmount(400000L)
                        .build()
        );

        mockPublishedResultLookup();

        CheckTicketsRequest req = new CheckTicketsRequest(1, drawDate, List.of("85"));
        CheckTicketResponse response = checkerService.checkTickets(req, null);

        assertThat(response.details().get(0).prize()).isEqualTo("G6");
        assertThat(response.details().get(0).amount()).isEqualTo(400000L);
    }

    @Test
    void totalQueriesIncremented_AfterCheck() {
        mockPublishedResultLookup();

        assertThat(publishedResult.getTotalQueries()).isEqualTo(0L);

        CheckTicketsRequest req = new CheckTicketsRequest(1, drawDate, List.of("123485"));
        checkerService.checkTickets(req, null);

        verify(resultRepository).incrementTotalQueries(1L);
    }

    @Test
    void historyRetrieval_AuthenticatedUser_ReturnsSessions() {
        when(userRepository.findByEmail("khach1@gmail.com"))
                .thenReturn(Optional.of(testUser));

        CheckHistory history1 = CheckHistory.builder()
                .ticketNumber("123485")
                .isWon(true)
                .wonPrize("G8")
                .wonAmount(100000L)
                .result(publishedResult)
                .build();

        CheckHistory history2 = CheckHistory.builder()
                .ticketNumber("000000")
                .isWon(false)
                .wonAmount(0L)
                .result(publishedResult)
                .build();

        CheckSession session = CheckSession.builder()
                .id(1L)
                .user(testUser)
                .totalSpent(20000L)
                .totalWon(100000L)
                .histories(new ArrayList<>(List.of(history1, history2)))
                .build();

        history1.setSession(session);
        history2.setSession(session);

        when(sessionRepository.findAllWithHistoriesByUserId(3L))
        .thenReturn(List.of(session));

        List<HistorySessionResponse> history =
                checkerService.getUserHistory("khach1@gmail.com");

        assertThat(history).hasSize(1);
        assertThat(history.get(0).totalSpent()).isEqualTo(20000L);
        assertThat(history.get(0).totalWon()).isEqualTo(100000L);
        assertThat(history.get(0).tickets()).hasSize(2);
    }
}
