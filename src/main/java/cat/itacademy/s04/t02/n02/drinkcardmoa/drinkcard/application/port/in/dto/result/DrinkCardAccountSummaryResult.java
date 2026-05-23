package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;

import java.time.Instant;

public record DrinkCardAccountSummaryResult(
        String volunteerId,
        int credits,
        String status,
        Instant lastPurchaseTimestamp
) {
    public static DrinkCardAccountSummaryResult from(DrinkCardAccount account) {
        return new DrinkCardAccountSummaryResult(
                account.getVolunteerId().asString(),
                account.getCredits(),
                account.getStatus().name(),
                account.getLastPurchaseTimestamp()
        );
    }
}
