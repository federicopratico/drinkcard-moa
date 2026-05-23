package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ExpirePendingDrinkTicketUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkTicket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ExpirePendingDrinkTicketService  implements ExpirePendingDrinkTicketUseCase {

    private final DrinkTicketRepository drinkTicketRepository;

    public ExpirePendingDrinkTicketService(DrinkTicketRepository drinkTicketRepository) {
        this.drinkTicketRepository = drinkTicketRepository;
    }

    @Transactional
    @Override
    public int execute() {
        Instant now = Instant.now();

        List<DrinkTicket> expiredTickets = drinkTicketRepository.findExpiredPendingTickets(now);

        expiredTickets.forEach(drinkTicket -> {
            drinkTicket.markAsExpired();
            drinkTicketRepository.save(drinkTicket);
        });

        return expiredTickets.size();
    }
}
