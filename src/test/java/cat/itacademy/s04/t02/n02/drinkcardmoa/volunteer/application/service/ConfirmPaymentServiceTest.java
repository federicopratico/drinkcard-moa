package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.ConfirmPaymentCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.ConfirmPaymentResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.VolunteerRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.payment.PaymentGateway;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.payment.PaymentGatewayStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.exception.PaymentNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.PaymentID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Volunteer;
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
    private VolunteerRepository volunteerRepository;

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

        verify(volunteerRepository, never()).findByVolunteerId(any());
        verify(paymentGateway, never()).fetchCheckoutStatus(any());
        verify(paymentRepository, never()).save(any());
        verify(volunteerRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_WhenPaymentIsAlreadyFinalized_ReturnsCurrentResultWithoutCallingGatewayOrSaving() {
        VolunteerID volunteerId = VolunteerID.generate();
        Payment payment = finalizedPayment(volunteerId, PaymentStatus.SUCCESS);
        Volunteer volunteer = Volunteer.rehydrate(1L, volunteerId, 10, Instant.now(), Instant.now());

        when(paymentRepository.findByPaymentId(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(volunteerRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(volunteer));

        ConfirmPaymentResult result = service.execute(new ConfirmPaymentCommand(payment.getPaymentId().asString()));

        assertEquals(payment.getPaymentId().asString(), result.paymentId());
        assertEquals(PaymentStatus.SUCCESS.name(), result.status());
        assertEquals(10, result.credits());
        assertEquals(Card.newCard().getPrice(), result.amount());

        verify(paymentGateway, never()).fetchCheckoutStatus(any());
        verify(paymentRepository, never()).save(any());
        verify(volunteerRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_WhenProviderStatusIsPaid_MarksPaymentAsSuccessAddsCreditsAndPublishesEvent() {
        VolunteerID volunteerId = VolunteerID.generate();
        Payment payment = pendingPayment(volunteerId);
        Volunteer volunteer = Volunteer.rehydrate(1L, volunteerId, 0, null, Instant.now());

        when(paymentRepository.findByPaymentId(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(volunteerRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(volunteer));
        when(paymentGateway.fetchCheckoutStatus(PROVIDER_CHECKOUT_ID)).thenReturn(PaymentGatewayStatus.PAID);
        when(volunteerRepository.save(volunteer)).thenReturn(volunteer);
        when(paymentRepository.save(payment)).thenReturn(payment);

        ConfirmPaymentResult result = service.execute(new ConfirmPaymentCommand(payment.getPaymentId().asString()));

        assertEquals(payment.getPaymentId().asString(), result.paymentId());
        assertEquals(PaymentStatus.SUCCESS.name(), result.status());
        assertEquals(5, result.credits());
        assertEquals(Card.newCard().getPrice(), result.amount());

        verify(paymentGateway).fetchCheckoutStatus(PROVIDER_CHECKOUT_ID);
        verify(volunteerRepository).save(volunteer);
        verify(paymentRepository).save(payment);
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    void execute_WhenProviderStatusIsFailed_MarksPaymentAsFailedWithoutAddingCredits() {
        VolunteerID volunteerId = VolunteerID.generate();
        Payment payment = pendingPayment(volunteerId);
        Volunteer volunteer = Volunteer.rehydrate(1L, volunteerId, 0, null, Instant.now());

        when(paymentRepository.findByPaymentId(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(volunteerRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(volunteer));
        when(paymentGateway.fetchCheckoutStatus(PROVIDER_CHECKOUT_ID)).thenReturn(PaymentGatewayStatus.FAILED);
        when(volunteerRepository.save(volunteer)).thenReturn(volunteer);
        when(paymentRepository.save(payment)).thenReturn(payment);

        ConfirmPaymentResult result = service.execute(new ConfirmPaymentCommand(payment.getPaymentId().asString()));

        assertEquals(payment.getPaymentId().asString(), result.paymentId());
        assertEquals(PaymentStatus.FAILED.name(), result.status());
        assertEquals(0, result.credits());
        assertEquals(Card.newCard().getPrice(), result.amount());

        verify(paymentGateway).fetchCheckoutStatus(PROVIDER_CHECKOUT_ID);
        verify(volunteerRepository).save(volunteer);
        verify(paymentRepository).save(payment);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_WhenProviderStatusIsExpired_MarksPaymentAsExpiredWithoutAddingCredits() {
        VolunteerID volunteerId = VolunteerID.generate();
        Payment payment = pendingPayment(volunteerId);
        Volunteer volunteer = Volunteer.rehydrate(1L, volunteerId, 0, null, Instant.now());

        when(paymentRepository.findByPaymentId(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(volunteerRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(volunteer));
        when(paymentGateway.fetchCheckoutStatus(PROVIDER_CHECKOUT_ID)).thenReturn(PaymentGatewayStatus.EXPIRED);
        when(volunteerRepository.save(volunteer)).thenReturn(volunteer);
        when(paymentRepository.save(payment)).thenReturn(payment);

        ConfirmPaymentResult result = service.execute(new ConfirmPaymentCommand(payment.getPaymentId().asString()));

        assertEquals(payment.getPaymentId().asString(), result.paymentId());
        assertEquals(PaymentStatus.EXPIRED.name(), result.status());
        assertEquals(0, result.credits());
        assertEquals(Card.newCard().getPrice(), result.amount());

        verify(paymentGateway).fetchCheckoutStatus(PROVIDER_CHECKOUT_ID);
        verify(volunteerRepository).save(volunteer);
        verify(paymentRepository).save(payment);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_WhenProviderStatusIsPending_KeepsPaymentPendingWithoutAddingCredits() {
        VolunteerID volunteerId = VolunteerID.generate();
        Payment payment = pendingPayment(volunteerId);
        Volunteer volunteer = Volunteer.rehydrate(1L, volunteerId, 0, null, Instant.now());

        when(paymentRepository.findByPaymentId(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(volunteerRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(volunteer));
        when(paymentGateway.fetchCheckoutStatus(PROVIDER_CHECKOUT_ID)).thenReturn(PaymentGatewayStatus.PENDING);
        when(volunteerRepository.save(volunteer)).thenReturn(volunteer);
        when(paymentRepository.save(payment)).thenReturn(payment);

        ConfirmPaymentResult result = service.execute(new ConfirmPaymentCommand(payment.getPaymentId().asString()));

        assertEquals(payment.getPaymentId().asString(), result.paymentId());
        assertEquals(PaymentStatus.PENDING.name(), result.status());
        assertEquals(0, result.credits());
        assertEquals(Card.newCard().getPrice(), result.amount());

        verify(paymentGateway).fetchCheckoutStatus(PROVIDER_CHECKOUT_ID);
        verify(volunteerRepository).save(volunteer);
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