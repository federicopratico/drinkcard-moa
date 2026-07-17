package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.dto.VolunteerProfile;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentSummaryResult(
        String paymentId,
        String volunteerId,
        String volunteerFirstName,
        String volunteerLastName,
        String volunteerEmail,
        BigDecimal amount,
        String status,
        String providerCheckoutId,
        String providerCheckoutUrl,
        Instant paidAt,
        Instant createdAt
) {
    public static final String UNKNOWN = "unknown";

    public static PaymentSummaryResult from(Payment payment) {
        return from(payment, null);
    }

    public static PaymentSummaryResult from(Payment payment, VolunteerProfile profile) {
        return new PaymentSummaryResult(
                payment.getPaymentId().asString(),
                payment.getVolunteerId().asString(),
                profile != null ? profile.firstName() : UNKNOWN,
                profile != null ? profile.lastName() : UNKNOWN,
                profile != null ? profile.email() : UNKNOWN,
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getProviderCheckoutId(),
                payment.getProviderCheckoutUrl(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
}
