package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query;

import java.time.Instant;

public record ListDrinkTicketsAdminQuery(
        String volunteerId,
        String status,
        Instant from,
        Instant to,
        int page,
        int size,
        String sort
) {
}
