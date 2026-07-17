package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkConsumption;

import java.math.BigDecimal;
import java.util.List;

public record AdminStatsResponse(
        long totalAvailableCredits,
        BigDecimal totalSuccessfulPaymentsAmount,
        BigDecimal totalSuccessfulPayments,
        long totalActiveCards,
        List<DrinkConsumption> drinkConsumptions,
        List<TopVolunteerResponse> topVolunteers
) {
}
