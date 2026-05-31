package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response;

import java.math.BigDecimal;

public record PaymentStatusResponse(
        String paymentId,
        String status,
        BigDecimal amount
) {
}
