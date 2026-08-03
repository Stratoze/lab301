package com.lottery.checker.controller;

import com.lottery.checker.dto.request.CheckTicketsRequest;
import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.CheckTicketResponse;
import com.lottery.checker.dto.response.HistorySessionResponse;
import com.lottery.checker.service.CheckerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckerControllerTest {

    @Mock
    private CheckerService checkerService;

    @InjectMocks
    private CheckerController checkerController;

    private final LocalDate drawDate = LocalDate.of(2023, 10, 23);

    private Authentication authenticatedUser(String email) {
        return new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void checkTickets_GuestSingleTicket_PassesNullEmailToService() {
        CheckTicketResponse mockResult = new CheckTicketResponse(
                new CheckTicketResponse.CheckSummary(10000L, 100000L),
                List.of(new CheckTicketResponse.CheckDetail("123485", true, "G8", 100000L))
        );
        when(checkerService.checkTickets(any(CheckTicketsRequest.class), isNull()))
                .thenReturn(mockResult);

        CheckTicketsRequest request = new CheckTicketsRequest(1, drawDate, List.of("123485"));
        ResponseEntity<ApiResponse<CheckTicketResponse>> response =
                checkerController.checkTickets(request, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(checkerService).checkTickets(request, null);
    }

    @Test
    void checkTickets_AnonymousAuthentication_ResolvesToGuest() {
        CheckTicketResponse mockResult = new CheckTicketResponse(
                new CheckTicketResponse.CheckSummary(10000L, 0L), List.of()
        );
        when(checkerService.checkTickets(any(CheckTicketsRequest.class), isNull()))
                .thenReturn(mockResult);

        Authentication anonymous = new UsernamePasswordAuthenticationToken(
                "anonymousUser", null, List.of());
        CheckTicketsRequest request = new CheckTicketsRequest(1, drawDate, List.of("123485"));

        checkerController.checkTickets(request, anonymous);

        verify(checkerService).checkTickets(request, null);
    }

    @Test
    void checkTickets_AuthUserMultipleTickets_PassesEmailToService() {
        CheckTicketResponse mockResult = new CheckTicketResponse(
                new CheckTicketResponse.CheckSummary(30000L, 0L), List.of()
        );
        when(checkerService.checkTickets(any(CheckTicketsRequest.class), eq("khach1@gmail.com")))
                .thenReturn(mockResult);

        CheckTicketsRequest request = new CheckTicketsRequest(1, drawDate, List.of("123485", "000000", "999999"));
        ResponseEntity<ApiResponse<CheckTicketResponse>> response =
                checkerController.checkTickets(request, authenticatedUser("khach1@gmail.com"));

        assertThat(response.getBody().isSuccess()).isTrue();
        verify(checkerService).checkTickets(request, "khach1@gmail.com");
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
        List<HistorySessionResponse> mockHistory = List.of(
                new HistorySessionResponse(1L, LocalDateTime.now(), 20000L, 100000L, List.of())
        );
        when(checkerService.getUserHistory("khach1@gmail.com"))
                .thenReturn(mockHistory);

        Principal principal = () -> "khach1@gmail.com";

        ResponseEntity<ApiResponse<List<HistorySessionResponse>>> response =
                checkerController.getHistory(principal);

        assertThat(response.getBody().getData()).hasSize(1);
        verify(checkerService).getUserHistory("khach1@gmail.com");
    }
}