package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response;

import java.math.BigDecimal;

public record ConfirmPaymentResponse(
        String paymentId,
        String status,
        int credits,
        BigDecimal amount
) {
}
