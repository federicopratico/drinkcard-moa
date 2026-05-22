package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicketStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;

import java.time.Instant;

public record DrinkTicketSearchCriteria(
        VolunteerID volunteerId,
        DrinkTicketStatus status,
        Instant from,
        Instant to,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
}
