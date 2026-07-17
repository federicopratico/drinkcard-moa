package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.AdminStatsResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.AdminStatsResponse;
import org.springframework.stereotype.Component;

@Component
public class AdminStatsControllerMapper {

    public AdminStatsResponse toResponse(AdminStatsResult result) {
        return new AdminStatsResponse(
                result.totalAvailableCredits(),
                result.totalSuccessfulPaymentsAmount(),
                result.totalActiveCards()
        );
    }
}
