package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result;

public record ConsumeDrinkTicketResult(
        String ticketId,
        String status,
        String drinkType,
        int remainingCredits
) {
}
