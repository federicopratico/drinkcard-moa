package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListDrinkTicketsAdminQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkTicketSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListDrinkTicketsAdminUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.DrinkTicketSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.PageResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper.AdminDrinkTicketMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDrinkTicketControllerTest {

    @Mock
    private ListDrinkTicketsAdminUseCase listDrinkTicketsAdminUseCase;

    private AdminDrinkTicketController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminDrinkTicketController(
                listDrinkTicketsAdminUseCase,
                new AdminDrinkTicketMapper()
        );
    }

    @Test
    void listDrinkTickets_WhenDrinkTicketsExist_ShouldReturnPagedDrinkTicketResponse() {
        String volunteerId = "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1";
        String staffId = "8799df50-d517-4693-9e46-51b537c305a2";
        Instant from = Instant.parse("2026-05-01T00:00:00Z");
        Instant to = Instant.parse("2026-05-19T23:59:59Z");
        Instant createdAt = Instant.parse("2026-05-19T20:00:00Z");
        Instant expiresAt = Instant.parse("2026-05-19T20:01:30Z");
        Instant consumedAt = Instant.parse("2026-05-19T20:01:00Z");

        DrinkTicketSummaryResult drinkTicket = new DrinkTicketSummaryResult(
                "7aab22f8-60d3-4700-8ba6-b35e67dfacb6",
                volunteerId,
                "BEER",
                "CONSUMED",
                createdAt,
                expiresAt,
                consumedAt,
                staffId
        );

        when(listDrinkTicketsAdminUseCase.execute(new ListDrinkTicketsAdminQuery(
                volunteerId,
                "CONSUMED",
                from,
                to,
                1,
                10,
                "createdAt,desc"
        ))).thenReturn(new PageResult<>(List.of(drinkTicket), 1, 10, 35, 4));

        ResponseEntity<PageResponse<DrinkTicketSummaryResponse>> response = controller.listDrinkTickets(
                volunteerId,
                "CONSUMED",
                from,
                to,
                1,
                10,
                "createdAt,desc"
        );

        ArgumentCaptor<ListDrinkTicketsAdminQuery> queryCaptor =
                ArgumentCaptor.forClass(ListDrinkTicketsAdminQuery.class);
        verify(listDrinkTicketsAdminUseCase).execute(queryCaptor.capture());

        PageResponse<DrinkTicketSummaryResponse> body = response.getBody();
        DrinkTicketSummaryResponse drinkTicketResponse = body.content().getFirst();
        ListDrinkTicketsAdminQuery query = queryCaptor.getValue();

        assertAll(
                () -> assertEquals(200, response.getStatusCode().value()),
                () -> assertNotNull(body),
                () -> assertEquals(1, body.page()),
                () -> assertEquals(10, body.size()),
                () -> assertEquals(35, body.totalElements()),
                () -> assertEquals(4, body.totalPages()),
                () -> assertEquals(drinkTicket.drinkTicketId(), drinkTicketResponse.drinkTicketId()),
                () -> assertEquals(drinkTicket.volunteerId(), drinkTicketResponse.volunteerId()),
                () -> assertEquals(drinkTicket.drinkType(), drinkTicketResponse.drinkType()),
                () -> assertEquals(drinkTicket.status(), drinkTicketResponse.status()),
                () -> assertEquals(drinkTicket.createdAt(), drinkTicketResponse.createdAt()),
                () -> assertEquals(drinkTicket.expiresAt(), drinkTicketResponse.expiresAt()),
                () -> assertEquals(drinkTicket.consumedAt(), drinkTicketResponse.consumedAt()),
                () -> assertEquals(drinkTicket.consumedByStaffId(), drinkTicketResponse.consumedByStaffId()),
                () -> assertEquals(volunteerId, query.volunteerId()),
                () -> assertEquals("CONSUMED", query.status()),
                () -> assertEquals(from, query.from()),
                () -> assertEquals(to, query.to()),
                () -> assertEquals(1, query.page()),
                () -> assertEquals(10, query.size()),
                () -> assertEquals("createdAt,desc", query.sort())
        );
    }

    @Test
    void listDrinkTickets_ShouldRequireAdminRole() throws NoSuchMethodException {
        Method method = AdminDrinkTicketController.class.getMethod(
                "listDrinkTickets",
                String.class,
                String.class,
                Instant.class,
                Instant.class,
                int.class,
                int.class,
                String.class
        );

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertAll(
                () -> assertNotNull(preAuthorize),
                () -> assertEquals("hasRole('ADMIN')", preAuthorize.value())
        );
    }
}
