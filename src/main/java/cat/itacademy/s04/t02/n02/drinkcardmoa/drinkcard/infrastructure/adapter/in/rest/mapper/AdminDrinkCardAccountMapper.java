package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.DrinkCardAccountSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public PageResponse<DrinkCardAccountSummaryResponse> toResponse(PageResult<DrinkCardAccountSummaryResult> result) {
        List<DrinkCardAccountSummaryResponse> content = result.content()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}
