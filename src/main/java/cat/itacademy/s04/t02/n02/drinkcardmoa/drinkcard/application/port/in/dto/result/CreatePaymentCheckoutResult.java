package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result;

import java.math.BigDecimal;

public record CreatePaymentCheckoutResult(
        String paymentId,
        String checkoutUrl,
        String status,
        BigDecimal amount
) {
}
