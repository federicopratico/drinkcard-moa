package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.PaymentID;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByPaymentId(PaymentID paymentID);
    Optional<Payment> findByProviderCheckoutId(String providerCheckoutId);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
