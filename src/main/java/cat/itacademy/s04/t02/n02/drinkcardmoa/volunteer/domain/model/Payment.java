package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.exception.InvalidPaymentStateException;

import java.math.BigDecimal;
import java.time.Instant;

public class Payment {
    private TransactionID transactionId;
    private VolunteerID volunteerId;
    private String idempotencyKey;
    private BigDecimal amount;
    private PaymentStatus status;
    private String providerCheckoutId;
    private Instant paidAt;
    private Instant createdAt;

    private Payment(
            TransactionID transactionId,
            VolunteerID volunteerId,
            String idempotencyKey,
            BigDecimal amount,
            PaymentStatus status,
            String providerCheckoutId,
            Instant paidAt,
            Instant createdAt) {
        this.transactionId = transactionId;
        this.volunteerId = volunteerId;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.status = status;
        this.providerCheckoutId = providerCheckoutId;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
    }

    public static Payment pending(VolunteerID volunteerId, BigDecimal amount, String idempotencyKey) {
        return new Payment(
                TransactionID.generate(),
                volunteerId,
                idempotencyKey,
                amount,
                PaymentStatus.PENDING,
                null,
                null,
                Instant.now()
        );
    }

    public static Payment rehydrate(
            TransactionID transactionId,
            VolunteerID volunteerId,
            String idempotencyKey,
            BigDecimal amount,
            PaymentStatus status,
            String providerCheckoutId,
            Instant paidAt,
            Instant createdAt
    ) {
        return new Payment(
                transactionId,
                volunteerId,
                idempotencyKey,
                amount,
                status,
                providerCheckoutId,
                paidAt,
                createdAt);
    }

    public void attachProviderCheckoutId(String providerCheckoutId) {
        if (this.providerCheckoutId != null && !providerCheckoutId.isBlank())
            throw new InvalidPaymentStateException("Payment already has a provider checkout ID");

        this.providerCheckoutId = providerCheckoutId;
    }

    public void markAsSuccess() {
        if (this.status != PaymentStatus.PENDING)
            throw new InvalidPaymentStateException("Payment is not in pending state");

        this.status = PaymentStatus.SUCCESS;
        this.paidAt = Instant.now();
    }

    public void markAsFailed() {
        if(this.status != PaymentStatus.PENDING)
            throw new InvalidPaymentStateException("Payment is not in pending state");

        this.status = PaymentStatus.FAILED;
    }

    // is it possible to pass from failed to expired?
    public void markAsExpired() {
        if(this.status != PaymentStatus.PENDING)
            throw new InvalidPaymentStateException("Payment is not in pending state");

        this.status = PaymentStatus.EXPIRED;
    }

    boolean isExpired() {
        return this.status == PaymentStatus.EXPIRED;
    }

    boolean isSuccess() {
        return this.status == PaymentStatus.SUCCESS;
    }

    boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }

    public TransactionID getTransactionId() {
        return transactionId;
    }
    public VolunteerID getVolunteerId() {
        return volunteerId;
    }
    public String getIdempotencyKey() {
        return idempotencyKey;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public PaymentStatus getStatus() {
        return status;
    }
    public String getProviderCheckoutId() {
        return providerCheckoutId;
    }
    public Instant getPaidAt() {
        return paidAt;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
