package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response;

import java.time.Instant;

public record DrinkTicketStatusResponse(
        String ticketId,
        String status,
        String drinkType,
        Instant expiresAt,
        Instant consumedAt
) {
}
