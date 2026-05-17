package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDrinkTicketRequest(
        @NotNull @NotBlank
        String volunteerId,
        @NotNull @NotBlank
        String drinkType
) {
}
