package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.scheduler;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ExpirePendingDrinkTicketUseCase;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DrinkTicketExpirationScheduler {

    private final ExpirePendingDrinkTicketUseCase expirePendingDrinkTicketUseCase;
    
}
