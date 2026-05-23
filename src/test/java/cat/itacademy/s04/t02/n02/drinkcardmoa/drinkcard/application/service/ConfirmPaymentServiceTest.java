package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.ConfirmPaymentCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConfirmPaymentResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGateway;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGatewayStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.PaymentNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmPaymentServiceTest {

    private static final String IDEMPOTENCY_KEY = "checkout-request-123";
    private static final String PROVIDER_CHECKOUT_ID = "checkout-123";
    private static final String PROVIDER_CHECOUT_URL = "https://checkout.com/checkout-123";

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private DrinkCardAccountRepository drinkCardAccountRepository;

    @InjectMocks
    private ConfirmPaymentService service;

    @Test
    void execute_WhenPaymentDoesNotExist_ThrowsPaymentNotFoundException() {
        PaymentID paymentId = PaymentID.generate();

        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.empty());

        assertThrows(
                PaymentNotFoundException.class,
                () -> service.execute(new ConfirmPaymentCommand(paymentId.asString()))
        );

        verify(drinkCardAccountRepository, never()).findByVolunteerId(any());
        verify(paymentGateway, never()).fetchCheckoutStatus(any());
        verify(paymentRepository, never()).save(any());
        verify(drinkCardAccountRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_WhenPaymentIsAlreadyFinalized_ReturnsCurrentResultWithoutCallingGatewayOrSaving() {
        VolunteerID volunteerId = VolunteerID.generate();
        Payment payment = finalizedPayment(volunteerId, PaymentStatus.SUCCESS);
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(1L, volunteerId, 10, Instant.now(), Instant.now());

        when(paymentRepository.findByPaymentId(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(drinkCardAccount));

        ConfirmPaymentResult result = service.execute(new ConfirmPaymentCommand(payment.getPaymentId().asString()));

        assertEquals(payment.getPaymentId().asString(), result.paymentId());
        assertEquals(PaymentStatus.SUCCESS.name(), result.status());
        assertEquals(10, result.credits());
        assertEquals(Card.newCard().getPrice(), result.amount());

        verify(paymentGateway, never()).fetchCheckoutStatus(any());
        verify(paymentRepository, never()).save(any());
        verify(drinkCardAccountRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_WhenProviderStatusIsPaid_MarksPaymentAsSuccessAddsCreditsAndPublishesEvent() {
        VolunteerID volunteerId = VolunteerID.generate();
        Payment payment = pendingPayment(volunteerId);
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(1L, volunteerId, 0, null, Instant.now());

        when(paymentRepository.findByPaymentId(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(drinkCardAccount));
        when(paymentGateway.fetchCheckoutStatus(PROVIDER_CHECKOUT_ID)).thenReturn(PaymentGatewayStatus.PAID);
        when(drinkCardAccountRepository.save(drinkCardAccount)).thenReturn(drinkCardAccount);
        when(paymentRepository.save(payment)).thenReturn(payment);

        ConfirmPaymentResult result = service.execute(new ConfirmPaymentCommand(payment.getPaymentId().asString()));

        assertEquals(payment.getPaymentId().asString(), result.paymentId());
        assertEquals(PaymentStatus.SUCCESS.name(), result.status());
        assertEquals(5, result.credits());
        assertEquals(Card.newCard().getPrice(), result.amount());

        verify(paymentGateway).fetchCheckoutStatus(PROVIDER_CHECKOUT_ID);
        verify(drinkCardAccountRepository).save(drinkCardAccount);
        verify(paymentRepository).save(payment);
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    void execute_WhenProviderStatusIsFailed_MarksPaymentAsFailedWithoutAddingCredits() {
        VolunteerID volunteerId = VolunteerID.generate();
        Payment payment = pendingPayment(volunteerId);
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(1L, volunteerId, 0, null, Instant.now());

        when(paymentRepository.findByPaymentId(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(drinkCardAccount));
        when(paymentGateway.fetchCheckoutStatus(PROVIDER_CHECKOUT_ID)).thenReturn(PaymentGatewayStatus.FAILED);
        when(drinkCardAccountRepository.save(drinkCardAccount)).thenReturn(drinkCardAccount);
        when(paymentRepository.save(payment)).thenReturn(payment);

        ConfirmPaymentResult result = service.execute(new ConfirmPaymentCommand(payment.getPaymentId().asString()));

        assertEquals(payment.getPaymentId().asString(), result.paymentId());
        assertEquals(PaymentStatus.FAILED.name(), result.status());
        assertEquals(0, result.credits());
        assertEquals(Card.newCard().getPrice(), result.amount());

        verify(paymentGateway).fetchCheckoutStatus(PROVIDER_CHECKOUT_ID);
        verify(drinkCardAccountRepository).save(drinkCardAccount);
        verify(paymentRepository).save(payment);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_WhenProviderStatusIsExpired_MarksPaymentAsExpiredWithoutAddingCredits() {
        VolunteerID volunteerId = VolunteerID.generate();
        Payment payment = pendingPayment(volunteerId);
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(1L, volunteerId, 0, null, Instant.now());

        when(paymentRepository.findByPaymentId(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(drinkCardAccount));
        when(paymentGateway.fetchCheckoutStatus(PROVIDER_CHECKOUT_ID)).thenReturn(PaymentGatewayStatus.EXPIRED);
        when(drinkCardAccountRepository.save(drinkCardAccount)).thenReturn(drinkCardAccount);
        when(paymentRepository.save(payment)).thenReturn(payment);

        ConfirmPaymentResult result = service.execute(new ConfirmPaymentCommand(payment.getPaymentId().asString()));

        assertEquals(payment.getPaymentId().asString(), result.paymentId());
        assertEquals(PaymentStatus.EXPIRED.name(), result.status());
        assertEquals(0, result.credits());
        assertEquals(Card.newCard().getPrice(), result.amount());

        verify(paymentGateway).fetchCheckoutStatus(PROVIDER_CHECKOUT_ID);
        verify(drinkCardAccountRepository).save(drinkCardAccount);
        verify(paymentRepository).save(payment);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_WhenProviderStatusIsPending_KeepsPaymentPendingWithoutAddingCredits() {
        VolunteerID volunteerId = VolunteerID.generate();
        Payment payment = pendingPayment(volunteerId);
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(1L, volunteerId, 0, null, Instant.now());

        when(paymentRepository.findByPaymentId(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(drinkCardAccount));
        when(paymentGateway.fetchCheckoutStatus(PROVIDER_CHECKOUT_ID)).thenReturn(PaymentGatewayStatus.PENDING);
        when(drinkCardAccountRepository.save(drinkCardAccount)).thenReturn(drinkCardAccount);
        when(paymentRepository.save(payment)).thenReturn(payment);

        ConfirmPaymentResult result = service.execute(new ConfirmPaymentCommand(payment.getPaymentId().asString()));

        assertEquals(payment.getPaymentId().asString(), result.paymentId());
        assertEquals(PaymentStatus.PENDING.name(), result.status());
        assertEquals(0, result.credits());
        assertEquals(Card.newCard().getPrice(), result.amount());

        verify(paymentGateway).fetchCheckoutStatus(PROVIDER_CHECKOUT_ID);
        verify(drinkCardAccountRepository).save(drinkCardAccount);
        verify(paymentRepository).save(payment);
        verify(eventPublisher, never()).publish(any());
    }

    private Payment pendingPayment(VolunteerID volunteerId) {
        Payment payment = Payment.pending(volunteerId, Card.newCard().getPrice(), IDEMPOTENCY_KEY);
        payment.attachProviderCheckoutId(PROVIDER_CHECKOUT_ID);
        return payment;
    }

    private Payment finalizedPayment(VolunteerID volunteerId, PaymentStatus status) {
        return Payment.rehydrate(
                PaymentID.generate(),
                volunteerId,
                IDEMPOTENCY_KEY,
                Card.newCard().getPrice(),
                status,
                PROVIDER_CHECKOUT_ID,
                PROVIDER_CHECOUT_URL,
                status == PaymentStatus.SUCCESS ? Instant.now() : null,
                Instant.now()
        );
    }
}