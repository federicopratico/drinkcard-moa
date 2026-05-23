package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkTicket;

import java.time.Instant;

public record DrinkTicketStatusResult(
        String ticketId,
        String status,
        String drinkType,
        Instant expiresAt,
        Instant consumedAt
) {
    public static DrinkTicketStatusResult from(DrinkTicket drinkTicket) {
        return new DrinkTicketStatusResult(
                drinkTicket.getDrinkTicketId().asString(),
                drinkTicket.getStatus().name(),
                drinkTicket.getDrinkType().name(),
                drinkTicket.getExpiresAt(),
                drinkTicket.getConsumedAt()
        );
    }
}
