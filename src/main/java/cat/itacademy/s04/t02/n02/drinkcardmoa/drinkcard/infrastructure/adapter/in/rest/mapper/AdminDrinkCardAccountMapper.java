package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.DrinkCardAccountSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class AdminDrinkCardAccountMapper {

    public DrinkCardAccountSummaryResponse toResponse(DrinkCardAccountSummaryResult result) {
        return new DrinkCardAccountSummaryResponse(
                result.volunteerId(),
                result.credits(),
                result.status(),
                result.lastPurchaseTimestamp()
        );
    }
}
