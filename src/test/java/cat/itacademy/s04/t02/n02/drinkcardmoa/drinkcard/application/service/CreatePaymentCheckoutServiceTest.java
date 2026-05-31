package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.config.PaymentProperties;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.testhelper.PaymentTestBuilder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreatePaymentCheckoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreatePaymentCheckoutResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.HostedCheckout;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.HostedCheckoutRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGateway;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountSuspendedException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.PurchaseLimitExceededException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkCardAccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePaymentCheckoutServiceTest {

    private static final String REDIRECT_URL = "http://localhost:3000/payment/success";
    private static final String WEBHOOK_RETURN_URL = "http://localhost:8080/api/v1/payments/sumup/webhook";
    private static final Duration CHECKOUT_EXPIRATION = Duration.ofMinutes(15);

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private DrinkCardAccountRepository drinkCardAccountRepository;

    private CreatePaymentCheckoutService service;

    @BeforeEach
    void setUp() {
        PaymentProperties paymentProperties = new PaymentProperties();
        paymentProperties.setCheckoutExpiration(CHECKOUT_EXPIRATION);
        paymentProperties.setWebhookReturnUrl(WEBHOOK_RETURN_URL);

        service = new CreatePaymentCheckoutService(
                paymentRepository,
                paymentGateway,
                drinkCardAccountRepository,
                paymentProperties
        );
    }

    @Test
    void execute_WhenPaymentExistsForIdempotencyKey_ReturnsExistingPaymentWithoutCreatingCheckout() {
        String idempotencyKey = PaymentTestBuilder.DEFAULT_IDEMPOTENCY_KEY;
        VolunteerID volunteerId = PaymentTestBuilder.DEFAULT_VOLUNTEER_ID;

        Payment existingPayment = PaymentTestBuilder.aPayment()
                        .build();

        when(paymentRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(existingPayment));

        CreatePaymentCheckoutResult result = service.execute(command(volunteerId));

        assertAll(
                () -> assertEquals(existingPayment.getPaymentId().asString(), result.paymentId()),
                () -> assertEquals(existingPayment.getStatus().name(), result.status()),
                () -> assertEquals(existingPayment.getAmount(), result.amount()),
                () -> assertEquals(existingPayment.getProviderCheckoutUrl(), result.checkoutUrl())
        );

        verify(paymentRepository, times(1)).findByIdempotencyKey(idempotencyKey);
        verify(drinkCardAccountRepository, never()).findByVolunteerId(any());
        verify(paymentGateway, never()).createHostedCheckout(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void execute_WhenDrinkCardAccountCanPurchase_CreatesPendingPaymentAndHostedCheckout() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(volunteerId);
        Instant providerCreatedAt = Instant.now();
        String idempotencyKey = PaymentTestBuilder.DEFAULT_IDEMPOTENCY_KEY;

        HostedCheckout hostedCheckout = new HostedCheckout(
                "checkout-123",
                "https://checkout.sumup.com/checkout-123",
                providerCreatedAt
        );

        when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
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
        assertEquals(WEBHOOK_RETURN_URL, request.returnUrl());
        assertNotNull(request.validUntil());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(2)).save(paymentCaptor.capture());

        Payment pendingPayment = paymentCaptor.getAllValues().get(0);
        Payment paymentWithProviderData = paymentCaptor.getAllValues().get(1);

        assertEquals(pendingPayment.getExpiresAt(), request.validUntil());
        assertEquals(providerCreatedAt, paymentWithProviderData.getProviderCreatedAt());
    }

    @Test
    void execute_WhenDrinkCardAccountDoesNotExist_ThrowsDrinkCardAccountNotFoundException() {
        VolunteerID volunteerId = VolunteerID.generate();
        String idempotencyKey = PaymentTestBuilder.DEFAULT_IDEMPOTENCY_KEY;

        when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.empty());

        assertThrows(DrinkCardAccountNotFoundException.class, () -> service.execute(command(volunteerId)));

        verify(paymentGateway, never()).createHostedCheckout(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void execute_WhenDrinkCardAccountCannotPurchase_ThrowsPurchaseLimitExceededException() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(1L, volunteerId, 0, Instant.now(), Instant.now());
        String idempotencyKey = PaymentTestBuilder.DEFAULT_IDEMPOTENCY_KEY;

        when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(drinkCardAccount));

        assertThrows(PurchaseLimitExceededException.class, () -> service.execute(command(volunteerId)));

        verify(paymentGateway, never()).createHostedCheckout(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void execute_WhenDrinkCardAccountIsSuspended_ThrowsDrinkCardAccountSuspendedException() {
        VolunteerID volunteerId = VolunteerID.generate();
        String idempotencyKey = PaymentTestBuilder.DEFAULT_IDEMPOTENCY_KEY;
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                volunteerId,
                0,
                null,
                Instant.now(),
                DrinkCardAccountStatus.SUSPENDED
        );

        when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(drinkCardAccount));

        assertThrows(DrinkCardAccountSuspendedException.class, () -> service.execute(command(volunteerId)));

        verify(paymentGateway, never()).createHostedCheckout(any());
        verify(paymentRepository, never()).save(any());
    }

    private CreatePaymentCheckoutCommand command(VolunteerID volunteerId) {
        return new CreatePaymentCheckoutCommand(
                volunteerId.asString(),
                REDIRECT_URL,
                PaymentTestBuilder.DEFAULT_IDEMPOTENCY_KEY
        );
    }
}
