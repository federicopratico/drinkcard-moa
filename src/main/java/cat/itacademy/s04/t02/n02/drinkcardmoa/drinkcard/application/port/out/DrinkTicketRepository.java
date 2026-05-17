package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicketID;

import java.util.Optional;

public interface DrinkTicketRepository {
    DrinkTicket save(DrinkTicket drinkTicket);
    Optional<DrinkTicket> findByDrinkTicketId(DrinkTicketID drinkTicketId);
}
