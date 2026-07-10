package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddDrinkCardRequest(
        @NotBlank
        String volunteerId
) {
}
