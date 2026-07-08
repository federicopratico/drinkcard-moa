package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListCurrentVolunteerDrinkTicketsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkTicketSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.DrinkTicketSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkTicketID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkTicketStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkType;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCurrentVolunteerDrinkTicketsServiceTest {

    @Mock
    private DrinkTicketRepository drinkTicketRepository;

    @InjectMocks
    private ListCurrentVolunteerDrinkTicketsService service;

    @Test
    void execute_WhenNoPaginationProvided_ShouldSearchAuthenticatedVolunteerDrinkTicketsWithDefaults() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkTicket drinkTicket = createDrinkTicket(volunteerId, DrinkTicketStatus.PENDING);

        when(drinkTicketRepository.searchVolunteerDrinkTickets(any(DrinkTicketSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(drinkTicket), 0, 20, 1, 1));

        PageResult<DrinkTicketSummaryResult> result = service.execute(
                new ListCurrentVolunteerDrinkTicketsQuery(volunteerId.asString(), -1, 0, null)
        );

        ArgumentCaptor<DrinkTicketSearchCriteria> criteriaCaptor =
                ArgumentCaptor.forClass(DrinkTicketSearchCriteria.class);
        verify(drinkTicketRepository).searchVolunteerDrinkTickets(criteriaCaptor.capture());

        DrinkTicketSearchCriteria criteria = criteriaCaptor.getValue();
        DrinkTicketSummaryResult ticketResult = result.content().getFirst();

        assertAll(
                () -> assertEquals(volunteerId, criteria.volunteerId()),
                () -> assertNull(criteria.status()),
                () -> assertNull(criteria.from()),
                () -> assertNull(criteria.to()),
                () -> assertEquals(0, criteria.page()),
                () -> assertEquals(20, criteria.size()),
                () -> assertEquals("createdAt", criteria.sortBy()),
                () -> assertEquals("desc", criteria.sortDirection()),
                () -> assertEquals(1, result.content().size()),
                () -> assertEquals(drinkTicket.getDrinkTicketId().asString(), ticketResult.drinkTicketId()),
                () -> assertEquals(volunteerId.asString(), ticketResult.volunteerId()),
                () -> assertEquals(drinkTicket.getDrinkType().name(), ticketResult.drinkType()),
                () -> assertEquals(drinkTicket.getStatus().name(), ticketResult.status()),
                () -> assertEquals(drinkTicket.getCreatedAt(), ticketResult.createdAt()),
                () -> assertEquals(drinkTicket.getExpiresAt(), ticketResult.expiresAt()),
                () -> assertEquals(drinkTicket.getConsumedAt(), ticketResult.consumedAt()),
                () -> assertEquals(drinkTicket.getConsumedByStaffId(), ticketResult.consumedByStaffId())
        );
    }

    @Test
    void execute_WhenPaginationProvided_ShouldPassParsedCriteriaToRepository() {
        VolunteerID volunteerId = VolunteerID.generate();

        when(drinkTicketRepository.searchVolunteerDrinkTickets(any(DrinkTicketSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(), 2, 10, 0, 0));

        service.execute(new ListCurrentVolunteerDrinkTicketsQuery(
                volunteerId.asString(),
                2,
                10,
                "consumedAt,asc"
        ));

        ArgumentCaptor<DrinkTicketSearchCriteria> criteriaCaptor =
                ArgumentCaptor.forClass(DrinkTicketSearchCriteria.class);
        verify(drinkTicketRepository).searchVolunteerDrinkTickets(criteriaCaptor.capture());

        DrinkTicketSearchCriteria criteria = criteriaCaptor.getValue();

        assertAll(
                () -> assertEquals(volunteerId, criteria.volunteerId()),
                () -> assertEquals(2, criteria.page()),
                () -> assertEquals(10, criteria.size()),
                () -> assertEquals("consumedAt", criteria.sortBy()),
                () -> assertEquals("asc", criteria.sortDirection())
        );
    }

    @Test
    void execute_WhenSizeExceedsMaximum_ShouldCapPageSize() {
        VolunteerID volunteerId = VolunteerID.generate();

        when(drinkTicketRepository.searchVolunteerDrinkTickets(any(DrinkTicketSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(), 0, 100, 0, 0));

        service.execute(new ListCurrentVolunteerDrinkTicketsQuery(
                volunteerId.asString(),
                0,
                150,
                "expiresAt,desc"
        ));

        ArgumentCaptor<DrinkTicketSearchCriteria> criteriaCaptor =
                ArgumentCaptor.forClass(DrinkTicketSearchCriteria.class);
        verify(drinkTicketRepository).searchVolunteerDrinkTickets(criteriaCaptor.capture());

        DrinkTicketSearchCriteria criteria = criteriaCaptor.getValue();

        assertAll(
                () -> assertEquals(100, criteria.size()),
                () -> assertEquals("expiresAt", criteria.sortBy()),
                () -> assertEquals("desc", criteria.sortDirection())
        );
    }

    @Test
    void execute_WhenSortFieldIsInvalid_ShouldThrowIllegalArgumentException() {
        VolunteerID volunteerId = VolunteerID.generate();

        ListCurrentVolunteerDrinkTicketsQuery query = new ListCurrentVolunteerDrinkTicketsQuery(
                volunteerId.asString(),
                0,
                20,
                "volunteerId,asc"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(query)
        );
    }

    private DrinkTicket createDrinkTicket(VolunteerID volunteerId, DrinkTicketStatus status) {
        Instant createdAt = Instant.parse("2026-05-19T20:00:00Z");
        Instant expiresAt = Instant.parse("2026-05-19T20:01:30Z");

        return DrinkTicket.rehydrate(
                DrinkTicketID.generate(),
                volunteerId,
                DrinkType.BEER,
                status,
                createdAt,
                expiresAt,
                null,
                null
        );
    }
}
