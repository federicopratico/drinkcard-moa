package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaPaymentRepository extends JpaRepository <PaymentJpaEntity, UUID> {
    Optional<PaymentJpaEntity> findByPaymentId(UUID paymentID);
    Optional<PaymentJpaEntity> findByProviderCheckoutId(String providerCheckoutId);
    Optional<PaymentJpaEntity> findByIdempotencyKey(String idempotencyKey);
}
