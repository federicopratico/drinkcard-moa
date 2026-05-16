package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.request;

public record CreateDrinkTicketRequest(
        String volunteerId,
        String drinkType
) {
}
