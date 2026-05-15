package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.response;

import java.math.BigDecimal;

public record CreatePaymentCheckoutResponse(
        String paymentId,
        String checkoutUrl,
        String status,
        BigDecimal amount

) {
}
