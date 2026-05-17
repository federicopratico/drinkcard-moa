package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreatePaymentCheckoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreatePaymentCheckoutResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.HostedCheckout;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.HostedCheckoutRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGateway;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.PurchaseLimitExceededException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePaymentCheckoutServiceTest {

    private static final String REDIRECT_URL = "http://localhost:3000/payment/success";
    private static final String IDEMPOTENCY_KEY = "checkout-request-123";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private DrinkCardAccountRepository drinkCardAccountRepository;

    @InjectMocks
    private CreatePaymentCheckoutService service;

    @Test
    void execute_WhenPaymentExistsForIdempotencyKey_ReturnsExistingPaymentWithoutCreatingCheckout() {
        VolunteerID volunteerId = VolunteerID.generate();
        Payment existingPayment = Payment.pending(volunteerId, Card.newCard().getPrice(), IDEMPOTENCY_KEY);
        existingPayment.attachProviderCheckoutUrl("checkout-existing");

        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(existingPayment));

        CreatePaymentCheckoutResult result = service.execute(command(volunteerId));

        assertEquals(existingPayment.getPaymentId().asString(), result.paymentId());;
        assertEquals("checkout-existing", result.checkoutUrl());
        assertEquals(PaymentStatus.PENDING.name(), result.status());
        assertEquals(Card.newCard().getPrice(), result.amount());

        verify(drinkCardAccountRepository, never()).findByVolunteerId(any());
        verify(paymentGateway, never()).createHostedCheckout(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void execute_WhenVolunteerCanPurchase_CreatesPendingPaymentAndHostedCheckout() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(volunteerId);

        HostedCheckout hostedCheckout = new HostedCheckout(
                "checkout-123",
                "https://checkout.sumup.com/checkout-123"
        );

        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(drinkCardAccount));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.createHostedCheckout(any(HostedCheckoutRequest.class))).thenReturn(hostedCheckout);

        CreatePaymentCheckoutResult result = service.execute(command(volunteerId));

        assertNotNull(result.paymentId());
        assertEquals("https://checkout.sumup.com/checkout-123", result.checkoutUrl());
        assertEquals(PaymentStatus.PENDING.name(), result.status());
        assertEquals(Card.newCard().getPrice(), result.amount());

        ArgumentCaptor<HostedCheckoutRequest> requestCaptor =
                ArgumentCaptor.forClass(HostedCheckoutRequest.class);

        verify(paymentGateway).createHostedCheckout(requestCaptor.capture());

        HostedCheckoutRequest request = requestCaptor.getValue();

        assertEquals(result.paymentId(), request.clientReferenceId());
        assertEquals(Card.newCard().getPrice(), request.amount());
        assertEquals("EUR", request.currency());
        assertEquals("Drink card - 5 credits", request.description());
        assertEquals(REDIRECT_URL, request.redirectUrl());

        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void execute_WhenVolunteerDoesNotExist_ThrowsDrinkCardAccountNotFoundException() {
        VolunteerID volunteerId = VolunteerID.generate();

        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.empty());

        assertThrows(DrinkCardAccountNotFoundException.class, () -> service.execute(command(volunteerId)));

        verify(paymentGateway, never()).createHostedCheckout(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void execute_WhenVolunteerCannotPurchase_ThrowsPurchaseLimitExceededException() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(1L, volunteerId, 0, Instant.now(), Instant.now());

        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(drinkCardAccount));

        assertThrows(PurchaseLimitExceededException.class, () -> service.execute(command(volunteerId)));

        verify(paymentGateway, never()).createHostedCheckout(any());
        verify(paymentRepository, never()).save(any());
    }

    private CreatePaymentCheckoutCommand command(VolunteerID volunteerId) {
        return new CreatePaymentCheckoutCommand(
                volunteerId.asString(),
                REDIRECT_URL,
                IDEMPOTENCY_KEY
        );
    }
}