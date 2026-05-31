package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;

import java.math.BigDecimal;

public record PaymentStatusResult(
        String paymentId,
        String status,
        BigDecimal amount
) {
    public static PaymentStatusResult from(Payment payment) {
        return new PaymentStatusResult(
                payment.getPaymentId().asString(),
                payment.getStatus().name(),
                payment.getAmount()
        );
    }
}
