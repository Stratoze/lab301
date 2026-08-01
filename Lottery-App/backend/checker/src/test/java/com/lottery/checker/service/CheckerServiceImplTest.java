package com.lottery.checker.service;

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
import java.util.Map;
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

        Map<String, Object> response = checkerService.checkTickets(
                1,
                drawDate,
                List.of("123485"),
                null
        );

        ArgumentCaptor<CheckSession> sessionCaptor =
                ArgumentCaptor.forClass(CheckSession.class);

        verify(sessionRepository).save(sessionCaptor.capture());

        CheckSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getUser()).isNull();
        assertThat(savedSession.getTotalSpent()).isEqualTo(10000L);
        assertThat(savedSession.getTotalWon()).isEqualTo(100000L);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details =
                (List<Map<String, Object>>) response.get("details");

        assertThat(details).hasSize(1);
        assertThat(details.get(0)).containsEntry("isWon", true);
        assertThat(details.get(0)).containsEntry("prize", "G8");
        assertThat(details.get(0)).containsEntry("amount", 100000L);
    }

    @Test
    void guestCheck_LosingTicket_ReturnsNoPrize() {
        mockPublishedResultLookup();

        Map<String, Object> response = checkerService.checkTickets(
                1,
                drawDate,
                List.of("000000"),
                null
        );

        ArgumentCaptor<CheckSession> sessionCaptor =
                ArgumentCaptor.forClass(CheckSession.class);

        verify(sessionRepository).save(sessionCaptor.capture());

        CheckSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getTotalSpent()).isEqualTo(10000L);
        assertThat(savedSession.getTotalWon()).isEqualTo(0L);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details =
                (List<Map<String, Object>>) response.get("details");

        assertThat(details.get(0)).containsEntry("isWon", false);
        assertThat(details.get(0)).containsEntry("prize", "None");
        assertThat(details.get(0)).containsEntry("amount", 0L);
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

        Map<String, Object> response = checkerService.checkTickets(
                1,
                drawDate,
                List.of("123485", "000000", "999999"),
                "khach1@gmail.com"
        );

        ArgumentCaptor<CheckSession> sessionCaptor =
                ArgumentCaptor.forClass(CheckSession.class);

        verify(sessionRepository).save(sessionCaptor.capture());

        CheckSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getTotalSpent()).isEqualTo(30000L);
        assertThat(savedSession.getTotalWon()).isEqualTo(100000L + 2000000000L);
        assertThat(savedSession.getHistories()).hasSize(3);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details =
                (List<Map<String, Object>>) response.get("details");

        assertThat(details).hasSize(3);
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

        Map<String, Object> response = checkerService.checkTickets(
                1,
                drawDate,
                List.of("85"),
                null
        );

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details =
                (List<Map<String, Object>>) response.get("details");

        assertThat(details.get(0)).containsEntry("prize", "G6");
        assertThat(details.get(0)).containsEntry("amount", 400000L);
    }

    @Test
    void totalQueriesIncremented_AfterCheck() {
        mockPublishedResultLookup();

        assertThat(publishedResult.getTotalQueries()).isEqualTo(0L);

        checkerService.checkTickets(
                1,
                drawDate,
                List.of("123485"),
                null
        );

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

        List<Map<String, Object>> history =
                checkerService.getUserHistory("khach1@gmail.com");

        assertThat(history).hasSize(1);
        assertThat(history.get(0)).containsEntry("totalSpent", 20000L);
        assertThat(history.get(0)).containsEntry("totalWon", 100000L);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tickets =
                (List<Map<String, Object>>) history.get(0).get("tickets");

        assertThat(tickets).hasSize(2);
    }
}