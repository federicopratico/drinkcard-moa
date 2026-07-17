package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result;

import java.math.BigDecimal;

public record AdminStatsResult(
        long totalAvailableCredits,
        BigDecimal totalSuccessfulPaymentsAmount,
        long totalActiveCards
) {
}
