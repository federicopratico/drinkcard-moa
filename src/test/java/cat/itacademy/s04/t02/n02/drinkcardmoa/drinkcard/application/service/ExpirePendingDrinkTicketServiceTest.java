package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkTicketStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkType;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpirePendingDrinkTicketServiceTest {

    @Mock
    private DrinkTicketRepository drinkTicketRepository;

    @InjectMocks
    private ExpirePendingDrinkTicketService service;

    @Test
    void execute_WhenExpiredPendingTicketsExist_ShouldMarkTicketsAsExpiredAndSaveThem() {
        DrinkTicket beerTicket = expiredPendingTicket(DrinkType.PILS_BEER);
        DrinkTicket waterTicket = expiredPendingTicket(DrinkType.WATER);

        when(drinkTicketRepository.findExpiredPendingTickets(any(Instant.class)))
                .thenReturn(List.of(beerTicket, waterTicket));

        int expiredCount = service.execute();

        ArgumentCaptor<DrinkTicket> drinkTicketCaptor = ArgumentCaptor.forClass(DrinkTicket.class);

        verify(drinkTicketRepository).findExpiredPendingTickets(any(Instant.class));
        verify(drinkTicketRepository, times(2)).save(drinkTicketCaptor.capture());

        List<DrinkTicket> savedTickets = drinkTicketCaptor.getAllValues();

        assertEquals(2, expiredCount);
        assertIterableEquals(List.of(beerTicket, waterTicket), savedTickets);
        assertEquals(DrinkTicketStatus.EXPIRED, beerTicket.getStatus());
        assertEquals(DrinkTicketStatus.EXPIRED, waterTicket.getStatus());
    }

    @Test
    void execute_WhenNoExpiredPendingTicketsExist_ShouldReturnZeroAndSaveNothing() {
        when(drinkTicketRepository.findExpiredPendingTickets(any(Instant.class)))
                .thenReturn(List.of());

        int expiredCount = service.execute();

        assertEquals(0, expiredCount);
        verify(drinkTicketRepository).findExpiredPendingTickets(any(Instant.class));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    private DrinkTicket expiredPendingTicket(DrinkType drinkType) {
        return DrinkTicket.pending(
                VolunteerID.generate(),
                drinkType,
                Instant.now().minusSeconds(120)
        );
    }
}
