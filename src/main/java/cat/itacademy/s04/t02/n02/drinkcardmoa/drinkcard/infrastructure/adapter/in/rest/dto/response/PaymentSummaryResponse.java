package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentSummaryResponse(
        String paymentId,
        String volunteerId,
        BigDecimal amount,
        String status,
        String providerCheckoutId,
        String providerCheckoutUrl,
        Instant paidAt,
        Instant createdAt
) {
}
