package com.lottery.checker.service;

import com.lottery.checker.entity.*;
import com.lottery.checker.repository.*;
import com.lottery.checker.service.impl.CheckerServiceImpl;
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
class CheckerServiceImplTest {

    @Mock private LotteryResultRepository resultRepository;
    @Mock private UserRepository userRepository;
    @Mock private CheckSessionRepository sessionRepository;
    @Mock private CheckHistoryRepository checkHistoryRepository;

    @InjectMocks
    private CheckerServiceImpl checkerService;

    private LotteryResult publishedResult;
    private LotteryStation station;
    private User testUser;

    @BeforeEach
    void setUp() {
        station = LotteryStation.builder()
                .id(1)
                .stationCode("SOU-HCM")
                .name("TP. H? Chi Minh")
                .region("SOUTH")
                .build();

        publishedResult = LotteryResult.builder()
                .id(1L)
                .resultCode("RES-HCM-23102023")
                .station(station)
                .drawDate(LocalDate.of(2023, 10, 23))
                .status("PUBLISH")
                .totalQueries(0L)
                .prizeDetails(new ArrayList<>())
                .build();

        // Add prize details: G8=85 (100k), G7=763 (200k), G_DB=999999 (2B)
        publishedResult.addPrizeDetail(PrizeDetail.builder()
                .prizeType("G8").winningNumber("85").rewardAmount(100000L).build());
        publishedResult.addPrizeDetail(PrizeDetail.builder()
                .prizeType("G7").winningNumber("763").rewardAmount(200000L).build());
        publishedResult.addPrizeDetail(PrizeDetail.builder()
                .prizeType("G_DB").winningNumber("999999").rewardAmount(2000000000L).build());

        testUser = User.builder()
                .id(3L)
                .userCode("USR-10-2023-00000003")
                .email("khach1@gmail.com")
                .fullName("Le V?n Tam")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();
    }

    // 1.1 Guest check - winning ticket returns correct prize
    @Test
    void guestCheck_WinningTicket_ReturnsCorrectPrize() {
        // Mock: search returns published result
        Page<LotteryResult> resultPage = new PageImpl<>(List.of(publishedResult));
        when(resultRepository.searchTickets(eq(1), any(), any(), isNull(), isNull()))
                .thenReturn(resultPage);

        // Guest: no user email
        Map<String, Object> response = checkerService.checkTickets(
                1, LocalDate.of(2023, 10, 23), List.of("123485"), null);

        // Verify session saved with user=null, total_spent=10000
        ArgumentCaptor<CheckSession> sessionCaptor = ArgumentCaptor.forClass(CheckSession.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        CheckSession savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getUser()).isNull();
        assertThat(savedSession.getTotalSpent()).isEqualTo(10000L);
        assertThat(savedSession.getTotalWon()).isEqualTo(100000L);

        // Verify response details
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) response.get("details");
        assertThat(details).hasSize(1);
        assertThat(details.get(0)).containsEntry("isWon", true);
        assertThat(details.get(0)).containsEntry("prize", "G8");
        assertThat(details.get(0)).containsEntry("amount", 100000L);
    }

    // 1.2 Guest check - losing ticket returns no prize
    @Test
    void guestCheck_LosingTicket_ReturnsNoPrize() {
        Page<LotteryResult> resultPage = new PageImpl<>(List.of(publishedResult));
        when(resultRepository.searchTickets(eq(1), any(), any(), isNull(), isNull()))
                .thenReturn(resultPage);

        Map<String, Object> response = checkerService.checkTickets(
                1, LocalDate.of(2023, 10, 23), List.of("000000"), null);

        ArgumentCaptor<CheckSession> sessionCaptor = ArgumentCaptor.forClass(CheckSession.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        CheckSession savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getTotalSpent()).isEqualTo(10000L);
        assertThat(savedSession.getTotalWon()).isEqualTo(0L);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) response.get("details");
        assertThat(details.get(0)).containsEntry("isWon", false);
        assertThat(details.get(0)).containsEntry("prize", "None");
        assertThat(details.get(0)).containsEntry("amount", 0L);
    }

    // 1.3 Bulk check - multi-ticket evaluates all
    @Test
    void bulkCheck_MultiTickets_EvaluatesAll() {
        Page<LotteryResult> resultPage = new PageImpl<>(List.of(publishedResult));
        when(resultRepository.searchTickets(eq(1), any(), any(), isNull(), isNull()))
                .thenReturn(resultPage);
        when(userRepository.findByEmail("khach1@gmail.com")).thenReturn(Optional.of(testUser));
        when(checkHistoryRepository.existsByUserAndResultAndTicket(anyLong(), anyLong(), anyString()))
                .thenReturn(false);

        // 3 tickets: one wins G8, one loses, one wins G_DB
        Map<String, Object> response = checkerService.checkTickets(
                1, LocalDate.of(2023, 10, 23),
                List.of("123485", "000000", "999999"),
                "khach1@gmail.com");

        ArgumentCaptor<CheckSession> sessionCaptor = ArgumentCaptor.forClass(CheckSession.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        CheckSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getTotalSpent()).isEqualTo(30000L);
        assertThat(savedSession.getTotalWon()).isEqualTo(100000L + 2000000000L);
        assertThat(savedSession.getHistories()).hasSize(3);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) response.get("details");
        assertThat(details).hasSize(3);
    }

    // 1.4 Best prize selection - multiple matches takes highest
    @Test
    void bestPrizeSelection_MultipleMatches_TakesHighest() {
        // Ticket ending with "85" matches G8 (100k). Also add G6 "85" (400k) to test best-prize.
        publishedResult.addPrizeDetail(PrizeDetail.builder()
                .prizeType("G6").winningNumber("85").rewardAmount(400000L).build());

        Page<LotteryResult> resultPage = new PageImpl<>(List.of(publishedResult));
        when(resultRepository.searchTickets(eq(1), any(), any(), isNull(), isNull()))
                .thenReturn(resultPage);

        Map<String, Object> response = checkerService.checkTickets(
                1, LocalDate.of(2023, 10, 23), List.of("85"), null);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) response.get("details");
        // Should pick G6 (400k) over G8 (100k)
        assertThat(details.get(0)).containsEntry("prize", "G6");
        assertThat(details.get(0)).containsEntry("amount", 400000L);
    }

    // 1.5 total_queries incremented after check
    @Test
    void totalQueriesIncremented_AfterCheck() {
        Page<LotteryResult> resultPage = new PageImpl<>(List.of(publishedResult));
        when(resultRepository.searchTickets(eq(1), any(), any(), isNull(), isNull()))
                .thenReturn(resultPage);

        assertThat(publishedResult.getTotalQueries()).isEqualTo(0L);

        checkerService.checkTickets(1, LocalDate.of(2023, 10, 23), List.of("123485"), null);

        // Verify result was saved with incremented total_queries
        ArgumentCaptor<LotteryResult> resultCaptor = ArgumentCaptor.forClass(LotteryResult.class);
        verify(resultRepository).save(resultCaptor.capture());
        assertThat(resultCaptor.getValue().getTotalQueries()).isEqualTo(1L);
    }

    // 1.6 History retrieval - authenticated user sees own sessions
    @Test
    void historyRetrieval_AuthenticatedUser_ReturnsSessions() {
        when(userRepository.findByEmail("khach1@gmail.com")).thenReturn(Optional.of(testUser));

        CheckHistory history1 = CheckHistory.builder()
                .ticketNumber("123485").isWon(true).wonPrize("G8").wonAmount(100000L)
                .result(publishedResult)
                .build();
        CheckHistory history2 = CheckHistory.builder()
                .ticketNumber("000000").isWon(false).wonAmount(0L)
                .result(publishedResult)
                .build();

        CheckSession session = CheckSession.builder()
                .id(1L).user(testUser).totalSpent(20000L).totalWon(100000L)
                .histories(new ArrayList<>(List.of(history1, history2)))
                .build();
        history1.setSession(session);
        history2.setSession(session);

        when(sessionRepository.findAllByUserIdOrderByCreatedAtDesc(3L))
                .thenReturn(List.of(session));

        List<Map<String, Object>> history = checkerService.getUserHistory("khach1@gmail.com");

        assertThat(history).hasSize(1);
        assertThat(history.get(0)).containsEntry("totalSpent", 20000L);
        assertThat(history.get(0)).containsEntry("totalWon", 100000L);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tickets = (List<Map<String, Object>>) history.get(0).get("tickets");
        assertThat(tickets).hasSize(2);
    }
}