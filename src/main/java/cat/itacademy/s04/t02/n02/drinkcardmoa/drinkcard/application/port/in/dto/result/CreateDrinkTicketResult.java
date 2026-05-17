package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicket;

import java.time.Instant;

public record CreateDrinkTicketResult(
        String ticketId,
        String drinkType,
        String status,
        Instant expiresAt
) {
    public static CreateDrinkTicketResult from(DrinkTicket drinkTicket) {
        return new CreateDrinkTicketResult(
                drinkTicket.getDrinkTicketId().asString(),
                drinkTicket.getDrinkType().toString(),
                drinkTicket.getStatus().toString(),
                drinkTicket.getExpiresAt()
        );
    }
}
