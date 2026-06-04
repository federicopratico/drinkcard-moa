package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.testhelper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

public class PaymentTestBuilder {

    private static final Duration EXPIRATION_TIME = Duration.ofMinutes(15);

    public static final PaymentID DEFAULT_PAYMENT_ID = PaymentID.generate();
    public static final VolunteerID DEFAULT_VOLUNTEER_ID = VolunteerID.generate();
    public static final String DEFAULT_IDEMPOTENCY_KEY = "checkout-request-123";
    public static final BigDecimal DEFAULT_AMOUNT = Card.newCard().getPrice();
    public static final PaymentStatus DEFAULT_STATUS = PaymentStatus.PENDING;
    public static final String DEFAULT_PROVIDER_CHECKOUT_ID = "checkout-123";
    public static final String DEFAULT_PROVIDER_CHECKOUT_URL = "https://checkout.example.test";
    public static final Instant DEFAULT_CREATED_AT = Instant.now();
    public static final Instant DEFAULT_EXPIRES_AT = DEFAULT_CREATED_AT.plus( EXPIRATION_TIME);

    private PaymentID paymentId = DEFAULT_PAYMENT_ID;
    private VolunteerID volunteerId = DEFAULT_VOLUNTEER_ID;
    private String idempotencyKey = DEFAULT_IDEMPOTENCY_KEY;
    private BigDecimal amount = DEFAULT_AMOUNT;
    private PaymentStatus status = DEFAULT_STATUS;
    private String providerCheckoutId = DEFAULT_PROVIDER_CHECKOUT_ID;
    private String providerCheckoutUrl = DEFAULT_PROVIDER_CHECKOUT_URL;
    private Instant paidAt = null;
    private Instant createdAt = DEFAULT_CREATED_AT;
    private Instant expiresAt = DEFAULT_EXPIRES_AT;
    private Instant providerCreatedAt = null;

    private PaymentTestBuilder() {}

    public static PaymentTestBuilder aPayment() {
        return new PaymentTestBuilder();
    }

    public PaymentTestBuilder withPaymentId(PaymentID paymentId) {
        this.paymentId = paymentId;
        return this;
    }

    public PaymentTestBuilder withVolunteerId(VolunteerID volunteerId) {
        this.volunteerId = volunteerId;
        return this;
    }

    public PaymentTestBuilder withIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
        return this;
    }

    public PaymentTestBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public PaymentTestBuilder withStatus(PaymentStatus status) {
        this.status = status;
        return this;
    }

    public PaymentTestBuilder withProviderCheckoutId(String providerCheckoutId) {
        this.providerCheckoutId = providerCheckoutId;
        return this;
    }

    public PaymentTestBuilder withProviderCheckoutUrl(String providerCheckoutUrl) {
        this.providerCheckoutUrl = providerCheckoutUrl;
        return this;
    }

    public PaymentTestBuilder withPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
        return this;
    }

    public PaymentTestBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public PaymentTestBuilder withExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public PaymentTestBuilder withProviderCreatedAt(Instant providerCreatedAt) {
        this.providerCreatedAt = providerCreatedAt;
        return this;
    }

    public Payment build() {
        return Payment.rehydrate(
                paymentId,
                volunteerId,
                idempotencyKey,
                amount,
                status,
                providerCheckoutId,
                providerCheckoutUrl,
                paidAt,
                createdAt,
                expiresAt,
                providerCreatedAt
        );
    }
}
