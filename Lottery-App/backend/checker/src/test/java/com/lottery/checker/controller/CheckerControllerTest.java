package com.lottery.checker.controller;

import com.lottery.checker.dto.request.CheckTicketsRequest;
import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.CheckTicketResponse;
import com.lottery.checker.dto.response.HistorySessionResponse;
import com.lottery.checker.exception.BadRequestException;
import com.lottery.checker.exception.ConflictException;
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

    @Test
    void checkTickets_GuestSingleTicket_ReturnsResult() {
        CheckTicketResponse mockResult = new CheckTicketResponse(
                new CheckTicketResponse.CheckSummary(10000L, 100000L),
                List.of(new CheckTicketResponse.CheckDetail("123485", true, "G8", 100000L))
        );
        when(checkerService.checkTickets(any(CheckTicketsRequest.class), isNull()))
                .thenReturn(mockResult);

        CheckTicketsRequest request = new CheckTicketsRequest(1, drawDate, List.of("123485"));
        ResponseEntity<ApiResponse<CheckTicketResponse>> response =
                checkerController.checkTickets(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(checkerService).checkTickets(request, null);
    }

    @Test
    void checkTickets_GuestMultipleTickets_ThrowsError() {
        CheckTicketsRequest request = new CheckTicketsRequest(1, drawDate, List.of("123485", "000000"));
        assertThatThrownBy(() -> checkerController.checkTickets(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Guests can only check one ticket");
    }

    @Test
    void checkTickets_AuthUserMultipleTickets_PassesEmailToService() {
        setAuthenticatedUser("khach1@gmail.com");
        CheckTicketResponse mockResult = new CheckTicketResponse(
                new CheckTicketResponse.CheckSummary(30000L, 0L), List.of()
        );
        when(checkerService.checkTickets(any(CheckTicketsRequest.class), eq("khach1@gmail.com")))
                .thenReturn(mockResult);

        CheckTicketsRequest request = new CheckTicketsRequest(1, drawDate, List.of("123485", "000000", "999999"));
        ResponseEntity<ApiResponse<CheckTicketResponse>> response = checkerController.checkTickets(request);

        assertThat(response.getBody().isSuccess()).isTrue();
        verify(checkerService).checkTickets(request, "khach1@gmail.com");
    }

    @Test
    void checkTickets_DuplicateTicketsInRequest_ThrowsError() {
        setAuthenticatedUser("khach1@gmail.com");
        CheckTicketsRequest request = new CheckTicketsRequest(1, drawDate, List.of("123485", "123485"));
        assertThatThrownBy(() -> checkerController.checkTickets(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Duplicate ticket number");
    }

    @Test
    void checkTickets_FutureDate_ThrowsError() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        CheckTicketsRequest request = new CheckTicketsRequest(1, futureDate, List.of("123485"));
        assertThatThrownBy(() -> checkerController.checkTickets(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be in the future");
    }

    @Test
    void getAvailableDates_ReturnsPublishedDates() {
        when(checkerService.getAvailableDates(1))
                .thenReturn(List.of(drawDate, LocalDate.of(2023, 10, 30)));

        ResponseEntity<ApiResponse<List<LocalDate>>> response =
                checkerController.getAvailableDates(1);

        assertThat(response.getBody().getData()).hasSize(2);
        assertThat(response.getBody().getData()).contains(drawDate);
    }

    @Test
    void getHistory_AuthenticatedUser_ReturnsHistory() {
        setAuthenticatedUser("khach1@gmail.com");
        List<HistorySessionResponse> mockHistory = List.of(
                new HistorySessionResponse(1L, java.time.LocalDateTime.now(), 20000L, 100000L, List.of())
        );
        when(checkerService.getUserHistory("khach1@gmail.com"))
                .thenReturn(mockHistory);

        ResponseEntity<ApiResponse<List<HistorySessionResponse>>> response =
                checkerController.getHistory();

        assertThat(response.getBody().getData()).hasSize(1);
        verify(checkerService).getUserHistory("khach1@gmail.com");
    }

    @Test
    void getHistory_UnauthenticatedUser_ThrowsError() {
        assertThatThrownBy(() -> checkerController.getHistory())
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    void getHistory_AnonymousUser_ThrowsError() {
        var anonAuth = new UsernamePasswordAuthenticationToken(
                "anonymousUser", null, List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(anonAuth);

        assertThatThrownBy(() -> checkerController.getHistory())
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Unauthorized");
    }
}
