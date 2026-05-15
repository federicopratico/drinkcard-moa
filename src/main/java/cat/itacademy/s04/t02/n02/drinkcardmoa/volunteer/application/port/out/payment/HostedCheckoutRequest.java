package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.payment;

import java.math.BigDecimal;

public record HostedCheckoutRequest(
        String clientReferenceId,
        BigDecimal amount,
        String currency,
        String description,
        String redirectUrl
) {
}
