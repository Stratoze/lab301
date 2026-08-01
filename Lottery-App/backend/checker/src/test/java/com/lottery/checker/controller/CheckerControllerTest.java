package com.lottery.checker.controller;

import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.service.CheckerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckerControllerTest {

    @Mock
    private CheckerService checkerService;

    @InjectMocks
    private CheckerController checkerController;

    private final LocalDate drawDate = LocalDate.of(2023, 10, 23);

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUser(String email) {
        var auth = new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // --- CN3: Guest checks single ticket ---

    @Test
    void checkTickets_GuestSingleTicket_ReturnsResult() {
        Map<String, Object> mockResult = Map.of(
                "summary", Map.of("totalSpent", 10000L, "totalWon", 100000L),
                "details", List.of(Map.of("number", "123485", "isWon", true, "prize", "G8"))
        );
        when(checkerService.checkTickets(eq(1), eq(drawDate), eq(List.of("123485")), isNull()))
                .thenReturn(mockResult);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                checkerController.checkTickets(1, drawDate, List.of("123485"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(checkerService).checkTickets(1, drawDate, List.of("123485"), null);
    }

    // --- CN3: Guest multi-ticket rejected ---

    @Test
    void checkTickets_GuestMultipleTickets_ThrowsError() {
        assertThatThrownBy(() ->
                checkerController.checkTickets(1, drawDate, List.of("123485", "000000")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Guests can only check one ticket");
    }

    // --- CN3: Authenticated user multi-ticket ---

    @Test
    void checkTickets_AuthUserMultipleTickets_PassesEmailToService() {
        setAuthenticatedUser("khach1@gmail.com");
        Map<String, Object> mockResult = Map.of(
                "summary", Map.of("totalSpent", 30000L, "totalWon", 0L),
                "details", List.of()
        );
        when(checkerService.checkTickets(
                eq(1), eq(drawDate),
                eq(List.of("123485", "000000", "999999")),
                eq("khach1@gmail.com")))
                .thenReturn(mockResult);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                checkerController.checkTickets(1, drawDate, List.of("123485", "000000", "999999"));

        assertThat(response.getBody().isSuccess()).isTrue();
        verify(checkerService).checkTickets(1, drawDate,
                List.of("123485", "000000", "999999"), "khach1@gmail.com");
    }

    // --- CN3: Validation - invalid ticket format ---

    @Test
    void checkTickets_NonSixDigitTicket_ThrowsError() {
        assertThatThrownBy(() ->
                checkerController.checkTickets(1, drawDate, List.of("12345")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("exactly 6 digits");
    }

    @Test
    void checkTickets_AlphabeticTicket_ThrowsError() {
        assertThatThrownBy(() ->
                checkerController.checkTickets(1, drawDate, List.of("ABC123")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("exactly 6 digits");
    }

    @Test
    void checkTickets_EmptyTicket_ThrowsError() {
        assertThatThrownBy(() ->
                checkerController.checkTickets(1, drawDate, List.of("  ")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cannot be empty");
    }

    // --- CN3: Validation - max 50 tickets ---

    @Test
    void checkTickets_MoreThan50Tickets_ThrowsError() {
        setAuthenticatedUser("khach1@gmail.com");
        List<String> tickets = new java.util.ArrayList<>();
        for (int i = 0; i < 51; i++) {
            tickets.add(String.format("%06d", i));
        }

        assertThatThrownBy(() ->
                checkerController.checkTickets(1, drawDate, tickets))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Maximum 50 tickets");
    }

    // --- CN3: Validation - duplicate tickets in request ---

    @Test
    void checkTickets_DuplicateTicketsInRequest_ThrowsError() {
        setAuthenticatedUser("khach1@gmail.com");

        assertThatThrownBy(() ->
                checkerController.checkTickets(1, drawDate, List.of("123485", "123485")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Duplicate ticket number");
    }

    // --- CN3: Validation - invalid station ---

    @Test
    void checkTickets_InvalidStationId_ThrowsError() {
        assertThatThrownBy(() ->
                checkerController.checkTickets(0, drawDate, List.of("123485")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid station");
    }

    // --- CN3: Validation - future date ---

    @Test
    void checkTickets_FutureDate_ThrowsError() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        assertThatThrownBy(() ->
                checkerController.checkTickets(1, futureDate, List.of("123485")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cannot be in the future");
    }

    // --- CN3: Validation - empty numbers list ---

    @Test
    void checkTickets_EmptyNumbersList_ThrowsError() {
        assertThatThrownBy(() ->
                checkerController.checkTickets(1, drawDate, List.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("At least one ticket");
    }

    // --- CN3: Available dates ---

    @Test
    void getAvailableDates_ReturnsPublishedDates() {
        when(checkerService.getAvailableDates(1))
                .thenReturn(List.of(drawDate, LocalDate.of(2023, 10, 30)));

        ResponseEntity<ApiResponse<List<LocalDate>>> response =
                checkerController.getAvailableDates(1);

        assertThat(response.getBody().getData()).hasSize(2);
        assertThat(response.getBody().getData()).contains(drawDate);
    }

    // --- CN3: History - authenticated ---

    @Test
    void getHistory_AuthenticatedUser_ReturnsHistory() {
        setAuthenticatedUser("khach1@gmail.com");
        List<Map<String, Object>> mockHistory = List.of(
                Map.of("id", 1L, "totalSpent", 20000L, "totalWon", 100000L)
        );
        when(checkerService.getUserHistory("khach1@gmail.com"))
                .thenReturn(mockHistory);

        ResponseEntity<ApiResponse<List<Map<String, Object>>>> response =
                checkerController.getHistory();

        assertThat(response.getBody().getData()).hasSize(1);
        verify(checkerService).getUserHistory("khach1@gmail.com");
    }

    // --- CN3: History - unauthenticated rejected ---

    @Test
    void getHistory_UnauthenticatedUser_ThrowsError() {
        // No authentication set in SecurityContext
        assertThatThrownBy(() -> checkerController.getHistory())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    void getHistory_AnonymousUser_ThrowsError() {
        var anonAuth = new UsernamePasswordAuthenticationToken(
                "anonymousUser", null, List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(anonAuth);

        assertThatThrownBy(() -> checkerController.getHistory())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }
}