package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentSummaryResult(
        String paymentId,
        String volunteerId,
        BigDecimal amount,
        String status,
        String providerCheckoutId,
        String providerCheckoutUrl,
        Instant paidAt,
        Instant createdAt
) {
    public static PaymentSummaryResult from(Payment payment) {
        return new PaymentSummaryResult(
                payment.getPaymentId().asString(),
                payment.getVolunteerId().asString(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getProviderCheckoutId(),
                payment.getProviderCheckoutUrl(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
}
