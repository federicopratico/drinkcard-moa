package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetCurrentDrinkCardAccountQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CurrentDrinkCardAccountResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.CurrentDrinkCardAccountResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.DrinkCardAccountSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
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

    public DrinkCardAccountSummaryResponse toResponse(DrinkCardAccountSummaryResult result) {
        return new DrinkCardAccountSummaryResponse(
                result.volunteerId(),
                result.credits(),
                result.status(),
                result.lastPurchaseTimestamp()
        );
    }

    public PageResponse<DrinkCardAccountSummaryResponse> toPageResponse(PageResult<DrinkCardAccountSummaryResult> result) {
        return new PageResponse<>(
                result.content().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}
