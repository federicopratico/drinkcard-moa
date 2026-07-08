package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetDrinkTicketStatusQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkTicketStatusResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkTicketNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkTicketID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkTicketStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkType;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDrinkTicketStatusServiceTest {

    @Mock
    private DrinkTicketRepository drinkTicketRepository;

    @InjectMocks
    private GetDrinkTicketStatusService service;

    @Test
    void execute_WhenDrinkTicketExists_ShouldReturnDrinkTicketStatus() {
        Instant createdAt = Instant.now();
        DrinkTicket drinkTicket = DrinkTicket.pending(
                VolunteerID.generate(),
                DrinkType.PILS_BEER,
                createdAt
        );

        GetDrinkTicketStatusQuery query = new GetDrinkTicketStatusQuery(
                drinkTicket.getDrinkTicketId().asString()
        );

        when(drinkTicketRepository.findByDrinkTicketId(DrinkTicketID.from(query.ticketId())))
                .thenReturn(Optional.of(drinkTicket));

        DrinkTicketStatusResult result = service.execute(query);

        verify(drinkTicketRepository).findByDrinkTicketId(DrinkTicketID.from(query.ticketId()));

        assertAll(
                () -> assertEquals(drinkTicket.getDrinkTicketId().asString(), result.ticketId()),
                () -> assertEquals(DrinkTicketStatus.PENDING.name(), result.status()),
                () -> assertEquals(DrinkType.PILS_BEER.name(), result.drinkType()),
                () -> assertEquals(createdAt.plusSeconds(90), result.expiresAt()),
                () -> assertNull(result.consumedAt())
        );
    }

    @Test
    void execute_WhenDrinkTicketDoesNotExist_ShouldThrowDrinkTicketNotFoundException() {
        DrinkTicketID drinkTicketId = DrinkTicketID.generate();
        GetDrinkTicketStatusQuery query = new GetDrinkTicketStatusQuery(drinkTicketId.asString());

        when(drinkTicketRepository.findByDrinkTicketId(DrinkTicketID.from(query.ticketId())))
                .thenReturn(Optional.empty());

        assertThrows(
                DrinkTicketNotFoundException.class,
                () -> service.execute(query)
        );

        verify(drinkTicketRepository).findByDrinkTicketId(DrinkTicketID.from(query.ticketId()));
    }
}
