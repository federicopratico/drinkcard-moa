package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.DrinkTicketSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicketID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DrinkTicketRepository {
    DrinkTicket save(DrinkTicket drinkTicket);
    Optional<DrinkTicket> findByDrinkTicketId(DrinkTicketID drinkTicketId);
    boolean existsActivePendingByVolunteerId(VolunteerID volunteerId, Instant now);
    List<DrinkTicket> findExpiredPendingTickets(Instant now);
    PageResult<DrinkTicket> searchAdminDrinkTickets(DrinkTicketSearchCriteria criteria);
}
