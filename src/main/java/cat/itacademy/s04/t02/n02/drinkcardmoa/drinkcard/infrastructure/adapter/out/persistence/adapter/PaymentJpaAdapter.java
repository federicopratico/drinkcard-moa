package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.adapter;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.PaymentID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.mapper.PaymentMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository.JpaPaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class PaymentJpaAdapter implements PaymentRepository {

    private final JpaPaymentRepository jpaPaymentRepository;
    private final PaymentMapper mapper;

    @Override
    public Payment save(Payment payment) {
        return mapper.toDomain(jpaPaymentRepository.save(mapper.toEntity(payment)));
    }

    @Override
    public Optional<Payment> findByPaymentId(PaymentID paymentID) {
        return jpaPaymentRepository.findByPaymentId(paymentID.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByProviderCheckoutId(String providerCheckoutId) {
        return jpaPaymentRepository.findByProviderCheckoutId(providerCheckoutId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return jpaPaymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(mapper::toDomain);
    }
}
