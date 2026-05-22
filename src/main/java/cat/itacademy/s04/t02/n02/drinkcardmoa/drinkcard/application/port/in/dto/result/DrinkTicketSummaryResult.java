package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result;

import java.time.Instant;

public record DrinkTicketSummaryResult(
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
