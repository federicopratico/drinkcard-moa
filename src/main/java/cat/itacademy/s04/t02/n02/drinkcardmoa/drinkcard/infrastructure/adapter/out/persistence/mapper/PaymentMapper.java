package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.PaymentID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentJpaEntity toEntity(Payment payment) {
        return PaymentJpaEntity.create(
                payment.getPaymentId().value(),
                payment.getVolunteerId().value(),
                payment.getIdempotencyKey(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getProviderCheckoutId(),
                payment.getProviderCheckoutUrl(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }

    public Payment toDomain(PaymentJpaEntity entity) {
        return Payment.rehydrate(
                PaymentID.from(entity.getPaymentId().toString()),
                VolunteerID.from(entity.getVolunteerId().toString()),
                entity.getIdempotencyKey(),
                entity.getAmount(),
                PaymentStatus.valueOf(entity.getStatus().toUpperCase()),
                entity.getProviderCheckoutId(),
                entity.getProviderCheckoutUrl(),
                entity.getPaidAt(),
                entity.getCreatedAt()
        );
    }
}
