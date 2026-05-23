package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.InvalidPaymentStateException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    private static final BigDecimal AMOUNT = BigDecimal.valueOf(10);
    private static final String IDEMPOTENCY_KEY = "payment-123";

    @Test
    void pending_ShouldCreatePaymentWithPendingStatus() {
        VolunteerID volunteerId = VolunteerID.generate();

        Instant beforeCreation = Instant.now();
        Payment payment = Payment.pending(volunteerId, AMOUNT, IDEMPOTENCY_KEY);
        Instant afterCreation = Instant.now();

        assertAll(
                () -> assertNotNull(payment.getPaymentId()),
                () -> assertEquals(volunteerId, payment.getVolunteerId()),
                () -> assertEquals(IDEMPOTENCY_KEY, payment.getIdempotencyKey()),
                () -> assertEquals(AMOUNT, payment.getAmount()),
                () -> Assertions.assertEquals(PaymentStatus.PENDING, payment.getStatus()),
                () -> assertNull(payment.getProviderCheckoutId()),
                () -> assertNull(payment.getProviderCheckoutUrl()),
                () -> assertNull(payment.getPaidAt()),
                () -> assertFalse(payment.getCreatedAt().isBefore(beforeCreation)),
                () -> assertFalse(payment.getCreatedAt().isAfter(afterCreation))
        );
    }

    @Test
    void rehydrate_ShouldRestorePaymentWithGivenValues() {
        PaymentID paymentId = PaymentID.generate();
        VolunteerID volunteerId = VolunteerID.generate();
        Instant paidAt = Instant.now();
        Instant createdAt = Instant.now();

        Payment payment = Payment.rehydrate(
                paymentId,
                volunteerId,
                IDEMPOTENCY_KEY,
                AMOUNT,
                PaymentStatus.SUCCESS,
                "checkout-id-123",
                "https://checkout.url",
                paidAt,
                createdAt
        );

        assertAll(
                () -> assertEquals(paymentId, payment.getPaymentId()),
                () -> assertEquals(volunteerId, payment.getVolunteerId()),
                () -> assertEquals(IDEMPOTENCY_KEY, payment.getIdempotencyKey()),
                () -> assertEquals(AMOUNT, payment.getAmount()),
                () -> assertEquals(PaymentStatus.SUCCESS, payment.getStatus()),
                () -> assertEquals("checkout-id-123", payment.getProviderCheckoutId()),
                () -> assertEquals("https://checkout.url", payment.getProviderCheckoutUrl()),
                () -> assertEquals(paidAt, payment.getPaidAt()),
                () -> assertEquals(createdAt, payment.getCreatedAt())
        );
    }

    @Test
    void attachProviderCheckoutId_WhenPaymentDoesNotHaveCheckoutId_ShouldAttachIt() {
        Payment payment = pendingPayment();

        payment.attachProviderCheckoutId("checkout-id-123");

        assertEquals("checkout-id-123", payment.getProviderCheckoutId());
    }

    @Test
    void attachProviderCheckoutId_WhenPaymentAlreadyHasCheckoutId_ShouldThrowException() {
        Payment payment = pendingPayment();
        payment.attachProviderCheckoutId("checkout-id-123");

        assertThrows(
                InvalidPaymentStateException.class,
                () -> payment.attachProviderCheckoutId("another-checkout-id")
        );
    }

    @Test
    void attachProviderCheckoutUrl_WhenPaymentDoesNotHaveCheckoutUrl_ShouldAttachIt() {
        Payment payment = pendingPayment();

        payment.attachProviderCheckoutUrl("https://checkout.url");

        assertEquals("https://checkout.url", payment.getProviderCheckoutUrl());
    }

    @Test
    void attachProviderCheckoutUrl_WhenPaymentAlreadyHasCheckoutUrl_ShouldThrowException() {
        Payment payment = pendingPayment();
        payment.attachProviderCheckoutUrl("https://checkout.url");

        assertThrows(
                InvalidPaymentStateException.class,
                () -> payment.attachProviderCheckoutUrl("https://another-checkout.url")
        );
    }

    @Test
    void markAsSuccess_WhenPaymentIsPending_ShouldChangeStatusToSuccessAndSetPaidAt() {
        Payment payment = pendingPayment();

        payment.markAsSuccess();

        assertAll(
                () -> assertEquals(PaymentStatus.SUCCESS, payment.getStatus()),
                () -> assertNotNull(payment.getPaidAt()),
                () -> assertTrue(payment.isSuccess()),
                () -> assertTrue(payment.isFinalized()),
                () -> assertFalse(payment.isFailed()),
                () -> assertFalse(payment.isExpired())
        );
    }

    @Test
    void markAsFailed_WhenPaymentIsPending_ShouldChangeStatusToFailed() {
        Payment payment = pendingPayment();

        payment.markAsFailed();

        assertAll(
                () -> assertEquals(PaymentStatus.FAILED, payment.getStatus()),
                () -> assertNull(payment.getPaidAt()),
                () -> assertTrue(payment.isFailed()),
                () -> assertTrue(payment.isFinalized()),
                () -> assertFalse(payment.isSuccess()),
                () -> assertFalse(payment.isExpired())
        );
    }

    @Test
    void markAsExpired_WhenPaymentIsPending_ShouldChangeStatusToExpired() {
        Payment payment = pendingPayment();

        payment.markAsExpired();

        assertAll(
                () -> assertEquals(PaymentStatus.EXPIRED, payment.getStatus()),
                () -> assertNull(payment.getPaidAt()),
                () -> assertTrue(payment.isExpired()),
                () -> assertTrue(payment.isFinalized()),
                () -> assertFalse(payment.isSuccess()),
                () -> assertFalse(payment.isFailed())
        );
    }

    @Test
    void markAsSuccess_WhenPaymentIsNotPending_ShouldThrowException() {
        Payment payment = paymentWithStatus(PaymentStatus.FAILED);

        assertThrows(InvalidPaymentStateException.class, payment::markAsSuccess);
    }

    @Test
    void markAsFailed_WhenPaymentIsNotPending_ShouldThrowException() {
        Payment payment = paymentWithStatus(PaymentStatus.SUCCESS);

        assertThrows(InvalidPaymentStateException.class, payment::markAsFailed);
    }

    @Test
    void markAsExpired_WhenPaymentIsNotPending_ShouldThrowException() {
        Payment payment = paymentWithStatus(PaymentStatus.SUCCESS);

        assertThrows(InvalidPaymentStateException.class, payment::markAsExpired);
    }

    @Test
    void isFinalized_WhenPaymentIsPending_ShouldReturnFalse() {
        Payment payment = pendingPayment();

        assertFalse(payment.isFinalized());
    }

    private static Payment pendingPayment() {
        return Payment.pending(
                VolunteerID.generate(),
                AMOUNT,
                IDEMPOTENCY_KEY
        );
    }

    private static Payment paymentWithStatus(PaymentStatus status) {
        return Payment.rehydrate(
                PaymentID.generate(),
                VolunteerID.generate(),
                IDEMPOTENCY_KEY,
                AMOUNT,
                status,
                null,
                null,
                null,
                Instant.now()
        );
    }
}

