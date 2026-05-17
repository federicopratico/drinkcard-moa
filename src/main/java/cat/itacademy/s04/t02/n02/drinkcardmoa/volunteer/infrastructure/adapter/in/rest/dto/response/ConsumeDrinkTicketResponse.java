package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.response;

public record ConsumeDrinkTicketResponse(
        String ticketId,
        String status,
        String drinkType,
        int remainingCredits
) {
}
