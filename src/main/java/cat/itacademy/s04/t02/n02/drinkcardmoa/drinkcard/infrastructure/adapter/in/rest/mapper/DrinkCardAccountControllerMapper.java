package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetCurrentDrinkCardAccountQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CurrentDrinkCardAccountResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.CurrentDrinkCardAccountResponse;
import org.springframework.stereotype.Component;

@Component
public class DrinkCardAccountControllerMapper {

    public GetCurrentDrinkCardAccountQuery toQuery(String userId) {
        return new GetCurrentDrinkCardAccountQuery(userId);
    }

    public CurrentDrinkCardAccountResponse toResponse(CurrentDrinkCardAccountResult result) {
        return new CurrentDrinkCardAccountResponse(
                result.volunteerId(),
                result.credits(),
                result.status(),
                result.lastPurchaseTimestamp()
        );
    }
}
