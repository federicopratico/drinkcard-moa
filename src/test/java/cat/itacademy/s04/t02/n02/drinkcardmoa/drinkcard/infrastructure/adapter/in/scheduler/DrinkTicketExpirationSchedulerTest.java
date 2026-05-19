package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.scheduler;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ExpirePendingDrinkTicketUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DrinkTicketExpirationSchedulerTest {

    @Mock
    private ExpirePendingDrinkTicketUseCase expirePendingDrinkTicketUseCase;

    @InjectMocks
    private DrinkTicketExpirationScheduler scheduler;

    @Test
    void expirePendingDrinkTickets_ShouldExecuteExpirePendingDrinkTicketUseCase() {
        scheduler.expirePendingDrinkTickets();

        verify(expirePendingDrinkTicketUseCase).execute();
    }
}
