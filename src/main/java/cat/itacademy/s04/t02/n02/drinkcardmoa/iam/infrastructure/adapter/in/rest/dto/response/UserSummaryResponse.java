package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;

public record UserSummaryResponse(
        String userId,
        String fullName,
        String email,
        String role,
        String status,
        DrinkCardAccountSummaryResult drinkCard
) {
}
