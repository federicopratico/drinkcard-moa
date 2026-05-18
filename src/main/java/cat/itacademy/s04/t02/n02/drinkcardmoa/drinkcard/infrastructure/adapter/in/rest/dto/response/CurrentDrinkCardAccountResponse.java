package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response;

import java.time.Instant;

public record CurrentDrinkCardAccountResponse(
        String volunteerId,
        int credits,
        String status,
        Instant lastPurchaseTimestamp
) {
}
