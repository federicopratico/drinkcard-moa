package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response;

import java.time.Instant;

public record CreateDrinkTicketResponse(
        String ticketId,
        String drinkType,
        String status,
        Instant expiresAt
) {
}
