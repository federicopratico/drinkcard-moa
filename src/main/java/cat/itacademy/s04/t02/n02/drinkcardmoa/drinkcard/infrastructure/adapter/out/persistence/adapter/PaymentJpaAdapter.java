package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.adapter;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.PaymentSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.mapper.PaymentMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository.JpaPaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.out.persistence.JpaSpecificationBuilder;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
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

    @Override
    public PageResult<Payment> searchAdminPayments(PaymentSearchCriteria criteria) {
        Sort.Direction direction = Sort.Direction.fromString(criteria.sortDirection());
        PageRequest pageRequest = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by(direction, criteria.sortBy())
        );

        Page<PaymentJpaEntity> page = jpaPaymentRepository.findAll(toSpecification(criteria), pageRequest);
        List<Payment> payments = page.getContent()
                .stream()
                .map(mapper::toDomain)
                .toList();

        return new PageResult<>(
                payments,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public PageResult<Payment> searchVolunteerPayments(PaymentSearchCriteria criteria) {
        Sort.Direction direction = Sort.Direction.fromString(criteria.sortDirection());
        PageRequest pageRequest = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by(direction, criteria.sortBy())
        );

        Page<PaymentJpaEntity> page = jpaPaymentRepository.findAll(toSpecification(criteria), pageRequest);

        List<Payment> payments = page.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new PageResult<>(
                payments,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }


    @Override
    public BigDecimal sumSuccessfulPaymentsAmount() {
        BigDecimal result = jpaPaymentRepository.sumSuccessfulPaymentsAmount();
        return result == null ? BigDecimal.ZERO : result;
    }

    private Specification<PaymentJpaEntity> toSpecification(PaymentSearchCriteria criteria) {
        return JpaSpecificationBuilder.<PaymentJpaEntity>builder()
                .equal("volunteerId", criteria.volunteerId() == null ? null : criteria.volunteerId().value())
                .equal("status", criteria.status() == null ? null : criteria.status().name())
                .greaterThanOrEqualTo("createdAt", criteria.from())
                .lessThanOrEqualTo("createdAt", criteria.to())
                .build();
    }
}
