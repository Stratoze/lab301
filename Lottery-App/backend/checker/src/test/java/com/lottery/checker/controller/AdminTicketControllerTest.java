package com.lottery.checker.controller;

import com.lottery.checker.dto.request.CreateTicketRequest;
import com.lottery.checker.dto.request.PrizeRequest;
import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.PrizeResponse;
import com.lottery.checker.dto.response.TicketResponse;
import com.lottery.checker.entity.LotteryStation;
import com.lottery.checker.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import com.lottery.checker.dto.request.UpdateTicketStatusRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminTicketControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private Principal principal;

    @InjectMocks
    private AdminTicketController adminTicketController;

    private TicketResponse publishedTicket;
    private TicketResponse unpublishedTicket;
    private LotteryStation stationHCM;

    @BeforeEach
    void setUp() {
        stationHCM = LotteryStation.builder()
                .id(1).stationCode("SOU-HCM")
                .name("TP. Ho Chi Minh").region("SOUTH")
                .build();

        publishedTicket = new TicketResponse(
                1L, "RES-HCM-23102023", "TP. Ho Chi Minh",
                LocalDate.of(2023, 10, 23), "PUBLISH", 150L,
                List.of(new PrizeResponse("G8", "85", 100000L),
                        new PrizeResponse("G_DB", "999999", 2000000000L))
        );
        unpublishedTicket = new TicketResponse(
                2L, "RES-DT-24102023", "Dong Thap",
                LocalDate.of(2024, 10, 24), "UNPUBLISH", 0L,
                List.of(new PrizeResponse("G8", "12", 100000L))
        );
    }

    // --- CN2: Get stations (public) ---

    @Test
    void getStations_ReturnsAllStations() {
        when(ticketService.getAllStations())
                .thenReturn(List.of(stationHCM));

        ResponseEntity<ApiResponse<List<LotteryStation>>> response =
                adminTicketController.getStations();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getName())
                .isEqualTo("TP. Ho Chi Minh");
    }

    // --- CN2: Search tickets with pagination ---

    @Test
    void getTickets_WithStationAndDateRange_ReturnsFiltered() {
        PagedResponse<TicketResponse> page = PagedResponse.<TicketResponse>builder()
                .content(List.of(publishedTicket))
                .page(0).size(10).totalElements(1).totalPages(1).last(true)
                .build();
        when(ticketService.searchTickets(
                eq(1), eq(LocalDate.of(2023, 10, 1)),
                eq(LocalDate.of(2023, 10, 31)), isNull(), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<ApiResponse<PagedResponse<TicketResponse>>> response =
                adminTicketController.getTickets(
                        1, LocalDate.of(2023, 10, 1),
                        LocalDate.of(2023, 10, 31), null, 0, 10);

        assertThat(response.getBody().getData().getContent()).hasSize(1);
        assertThat(response.getBody().getData().getContent().get(0).status())
                .isEqualTo("PUBLISH");
    }

    @Test
    void getTickets_DefaultPagination_UsesPage0Size10() {
        PagedResponse<TicketResponse> page = PagedResponse.<TicketResponse>builder()
                .content(List.of(publishedTicket, unpublishedTicket))
                .page(0).size(10).totalElements(2).totalPages(1).last(true)
                .build();
        when(ticketService.searchTickets(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<ApiResponse<PagedResponse<TicketResponse>>> response =
                adminTicketController.getTickets(null, null, null, null, 0, 10);

        assertThat(response.getBody().getData().getContent()).hasSize(2);
    }

    // --- CN2: Create ticket ---

    @Test
    void createTicket_ValidRequest_ReturnsCreatedTicket() {
        CreateTicketRequest request = new CreateTicketRequest(
                1, LocalDate.of(2023, 10, 23),
                List.of(new PrizeRequest("G8", "85", 100000L),
                        new PrizeRequest("G_DB", "999999", 2000000000L)),
                null
        );
        when(principal.getName()).thenReturn("admin@veso.vn");
        when(ticketService.createTicket(request, "admin@veso.vn"))
                .thenReturn(publishedTicket);

        ResponseEntity<ApiResponse<TicketResponse>> response =
                adminTicketController.createTicket(request, principal);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getData().resultCode())
                .isEqualTo("RES-HCM-23102023");
        assertThat(response.getBody().getData().prizes()).hasSize(2);
    }

    // --- CN2: Update ticket ---

    @Test
    void updateTicket_ValidRequest_ReturnsUpdatedTicket() {
        CreateTicketRequest request = new CreateTicketRequest(
                1, LocalDate.of(2023, 10, 23),
                List.of(new PrizeRequest("G8", "99", 100000L)),
                "PUBLISH"
        );
        when(principal.getName()).thenReturn("admin@veso.vn");
        when(ticketService.updateTicket(1L, request, "admin@veso.vn"))
                .thenReturn(publishedTicket);

        ResponseEntity<ApiResponse<TicketResponse>> response =
                adminTicketController.updateTicket(1L, request, principal);

        assertThat(response.getBody().getData().id()).isEqualTo(1L);
        verify(ticketService).updateTicket(1L, request, "admin@veso.vn");
    }

    // --- CN2: Publish / Unpublish ---

    @Test
    void updateStatus_Publish_CallsServiceWithAdminEmail() {
        when(principal.getName()).thenReturn("admin@veso.vn");

        ResponseEntity<ApiResponse<String>> response =
                adminTicketController.updateStatus(1L, new UpdateTicketStatusRequest("PUBLISH"), principal);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getData()).contains("successfully");
        verify(ticketService).updateStatus(eq(1L), eq(new UpdateTicketStatusRequest("PUBLISH")), eq("admin@veso.vn"));
    }

    @Test
    void updateStatus_Unpublish_CallsServiceCorrectly() {
        when(principal.getName()).thenReturn("admin@veso.vn");

        ResponseEntity<ApiResponse<String>> response =
                adminTicketController.updateStatus(2L, new UpdateTicketStatusRequest("UNPUBLISH"), principal);

        verify(ticketService).updateStatus(eq(2L), eq(new UpdateTicketStatusRequest("UNPUBLISH")), eq("admin@veso.vn"));
    }
}