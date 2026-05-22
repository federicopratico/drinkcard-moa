package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response;

import java.time.Instant;

public record DrinkTicketSummaryResponse(
        String drinkTicketId,
        String volunteerId,
        String drinkType,
        String status,
        Instant createdAt,
        Instant expiresAt,
        Instant consumedAt,
        String consumedByStaffId
) {
}
