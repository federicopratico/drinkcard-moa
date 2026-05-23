package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkTicket;

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
    public static DrinkTicketSummaryResult from(DrinkTicket drinkTicket) {
        return new DrinkTicketSummaryResult(
                drinkTicket.getDrinkTicketId().asString(),
                drinkTicket.getVolunteerId().asString(),
                drinkTicket.getDrinkType().name(),
                drinkTicket.getStatus().name(),
                drinkTicket.getCreatedAt(),
                drinkTicket.getExpiresAt(),
                drinkTicket.getConsumedAt(),
                drinkTicket.getConsumedByStaffId()
        );
    }
}
