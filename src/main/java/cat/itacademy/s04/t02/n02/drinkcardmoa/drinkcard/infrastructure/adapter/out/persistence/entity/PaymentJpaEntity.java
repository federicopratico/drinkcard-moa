package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class PaymentJpaEntity {

    @Id
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "volunteer_id", nullable = false)
    private UUID volunteerId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "provider_checkout_id", unique = true)
    private String providerCheckoutId;

    @Column(name = "provider_checkout_url")
    private String providerCheckoutUrl;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "provider_created_at")
    private Instant providerCreatedAt;

    private PaymentJpaEntity(UUID paymentId, UUID volunteerId, String idempotencyKey, BigDecimal amount, String status, String providerCheckoutId, String providerCheckoutUrl,Instant paidAt, Instant createdAt, Instant expiresAt, Instant providerCreatedAt) {
        this.paymentId = paymentId;
        this.volunteerId = volunteerId;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.status = status;
        this.providerCheckoutId = providerCheckoutId;
        this.providerCheckoutUrl = providerCheckoutUrl;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.providerCreatedAt = providerCreatedAt;
    }

    public static PaymentJpaEntity create(UUID paymentId, UUID volunteerId, String idempotencyKey, BigDecimal amount, String status, String providerCheckoutId, String providerCheckoutUrl, Instant paidAt, Instant createdAt, Instant expiresAt, Instant providerCreatedAt) {
        return new PaymentJpaEntity(paymentId, volunteerId, idempotencyKey, amount, status, providerCheckoutId, providerCheckoutUrl, paidAt, createdAt, expiresAt, providerCreatedAt);
    }
}
