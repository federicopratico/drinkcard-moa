package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreatePaymentCheckoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreatePaymentCheckoutResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.HostedCheckout;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.HostedCheckoutRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGateway;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountSuspendedException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.PurchaseLimitExceededException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkCardAccountStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.config.PaymentProperties;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.testhelper.PaymentTestBuilder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private LockRegistry lockRegistry;

    @Mock
    private Lock lock;

    private CreatePaymentCheckoutService service;

    @BeforeEach
    void setUp() {
        PaymentProperties paymentProperties = new PaymentProperties();
        paymentProperties.setCheckoutExpiration(CHECKOUT_EXPIRATION);
        paymentProperties.setWebhookReturnUrl(WEBHOOK_RETURN_URL);

        when(lockRegistry.obtain(any())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);

        service = new CreatePaymentCheckoutService(
                paymentRepository,
                paymentGateway,
                drinkCardAccountRepository,
                transactionTemplate,
                paymentProperties,
                lockRegistry
        );
    }

    @Test
    void execute_WhenPaymentExistsForVolunteerToday_ReturnsExistingPaymentWithoutCreatingCheckout() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(volunteerId);
        Payment existingPayment = PaymentTestBuilder.aPayment()
                .withVolunteerId(volunteerId)
                .build();

        when(transactionTemplate.execute(any())).thenAnswer(executeTransactionCallback());
        doAnswer(executeWithoutResultCallback()).when(transactionTemplate).executeWithoutResult(any());
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(drinkCardAccount));
        when(paymentRepository.findByIdempotencyKey(any()))
                .thenReturn(Optional.of(existingPayment));

        CreatePaymentCheckoutResult result = service.execute(command(volunteerId));

        assertAll(
                () -> assertEquals(existingPayment.getPaymentId().asString(), result.paymentId()),
                () -> assertEquals(existingPayment.getStatus().name(), result.status()),
                () -> assertEquals(existingPayment.getAmount(), result.amount()),
                () -> assertEquals(existingPayment.getProviderCheckoutUrl(), result.checkoutUrl())
        );

        verify(paymentRepository).findByIdempotencyKey(argThat(key -> key.startsWith(volunteerId.asString())));
        verify(paymentGateway, never()).createHostedCheckout(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void execute_WhenDrinkCardAccountCanPurchase_CreatesHostedCheckoutAndSavesCompletePaymentOnce() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(volunteerId);
        Instant providerCreatedAt = Instant.now();
        HostedCheckout hostedCheckout = hostedCheckout(providerCreatedAt);

        when(transactionTemplate.execute(any())).thenAnswer(executeTransactionCallback());
        doAnswer(executeWithoutResultCallback()).when(transactionTemplate).executeWithoutResult(any());
        when(paymentRepository.findByIdempotencyKey(any()))
                .thenReturn(Optional.empty());
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(drinkCardAccount));
        when(paymentGateway.createHostedCheckout(any(HostedCheckoutRequest.class)))
                .thenReturn(hostedCheckout);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreatePaymentCheckoutResult result = service.execute(command(volunteerId));

        assertAll(
                () -> assertNotNull(result.paymentId()),
                () -> assertEquals(hostedCheckout.checkoutUrl(), result.checkoutUrl()),
                () -> assertEquals(PaymentStatus.PENDING.name(), result.status()),
                () -> assertEquals(Card.newCard().getPrice(), result.amount())
        );

        ArgumentCaptor<HostedCheckoutRequest> requestCaptor =
                ArgumentCaptor.forClass(HostedCheckoutRequest.class);
        verify(paymentGateway).createHostedCheckout(requestCaptor.capture());

        HostedCheckoutRequest request = requestCaptor.getValue();

        assertAll(
                () -> assertEquals(result.paymentId(), request.clientReferenceId()),
                () -> assertEquals(Card.newCard().getPrice(), request.amount()),
                () -> assertEquals("EUR", request.currency()),
                () -> assertEquals("Drink card - 5 credits", request.description()),
                () -> assertEquals(REDIRECT_URL, request.redirectUrl()),
                () -> assertEquals(WEBHOOK_RETURN_URL, request.returnUrl()),
                () -> assertNotNull(request.validUntil())
        );

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(1)).save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();

        assertAll(
                () -> assertEquals(request.clientReferenceId(), savedPayment.getPaymentId().asString()),
                () -> assertEquals(request.validUntil(), savedPayment.getExpiresAt()),
                () -> assertEquals(hostedCheckout.providerCheckoutId(), savedPayment.getProviderCheckoutId()),
                () -> assertEquals(hostedCheckout.checkoutUrl(), savedPayment.getProviderCheckoutUrl()),
                () -> assertEquals(providerCreatedAt, savedPayment.getProviderCreatedAt())
        );

        verify(drinkCardAccountRepository, times(1)).findByVolunteerId(volunteerId);
    }

    @Test
    void execute_WhenConcurrentRequestAlreadySavedPayment_ReturnsConcurrentlySavedPayment() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(volunteerId);
        Payment concurrentlySavedPayment = PaymentTestBuilder.aPayment()
                .withVolunteerId(volunteerId)
                .build();

        when(transactionTemplate.execute(any())).thenAnswer(executeTransactionCallback());
        doAnswer(executeWithoutResultCallback()).when(transactionTemplate).executeWithoutResult(any());
        when(paymentRepository.findByIdempotencyKey(any()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(concurrentlySavedPayment));
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(drinkCardAccount));
        when(paymentGateway.createHostedCheckout(any(HostedCheckoutRequest.class)))
                .thenReturn(hostedCheckout(Instant.now()));
        when(paymentRepository.save(any(Payment.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate idempotency key"));

        CreatePaymentCheckoutResult result = service.execute(command(volunteerId));

        assertAll(
                () -> assertEquals(concurrentlySavedPayment.getPaymentId().asString(), result.paymentId()),
                () -> assertEquals(concurrentlySavedPayment.getProviderCheckoutUrl(), result.checkoutUrl()),
                () -> assertEquals(concurrentlySavedPayment.getStatus().name(), result.status()),
                () -> assertEquals(concurrentlySavedPayment.getAmount(), result.amount())
        );

        verify(paymentGateway).createHostedCheckout(any(HostedCheckoutRequest.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentRepository, times(2)).findByIdempotencyKey(argThat(key -> key.startsWith(volunteerId.asString())));
    }

    @Test
    void execute_WhenSaveFailsAndNoConcurrentPaymentExists_RethrowsDataIntegrityViolationException() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(volunteerId);
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("unexpected constraint violation");

        when(transactionTemplate.execute(any())).thenAnswer(executeTransactionCallback());
        doAnswer(executeWithoutResultCallback()).when(transactionTemplate).executeWithoutResult(any());
        when(paymentRepository.findByIdempotencyKey(any()))
                .thenReturn(Optional.empty());
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(drinkCardAccount));
        when(paymentGateway.createHostedCheckout(any(HostedCheckoutRequest.class)))
                .thenReturn(hostedCheckout(Instant.now()));
        when(paymentRepository.save(any(Payment.class))).thenThrow(exception);

        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> service.execute(command(volunteerId))
        );

        assertEquals(exception, thrown);
        verify(paymentRepository, times(2)).findByIdempotencyKey(argThat(key -> key.startsWith(volunteerId.asString())));
    }

    @Test
    void execute_WhenDrinkCardAccountDoesNotExist_ThrowsDrinkCardAccountNotFoundException() {
        VolunteerID volunteerId = VolunteerID.generate();

        doAnswer(executeWithoutResultCallback()).when(transactionTemplate).executeWithoutResult(any());
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.empty());

        assertThrows(DrinkCardAccountNotFoundException.class, () -> service.execute(command(volunteerId)));

        verify(paymentGateway, never()).createHostedCheckout(any());
        verify(paymentRepository, never()).save(any());
        verify(paymentRepository, never()).findByIdempotencyKey(any());
    }

    @Test
    void execute_WhenDrinkCardAccountCannotPurchase_ThrowsPurchaseLimitExceededException() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount =
                DrinkCardAccount.rehydrate(1L, volunteerId, 0, Instant.now(), Instant.now());

        doAnswer(executeWithoutResultCallback()).when(transactionTemplate).executeWithoutResult(any());
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(drinkCardAccount));

        assertThrows(PurchaseLimitExceededException.class, () -> service.execute(command(volunteerId)));

        verify(paymentGateway, never()).createHostedCheckout(any());
        verify(paymentRepository, never()).save(any());
        verify(paymentRepository, never()).findByIdempotencyKey(any());
    }

    @Test
    void execute_WhenDrinkCardAccountIsSuspended_ThrowsDrinkCardAccountSuspendedException() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                volunteerId,
                0,
                null,
                Instant.now(),
                DrinkCardAccountStatus.SUSPENDED
        );

        doAnswer(executeWithoutResultCallback()).when(transactionTemplate).executeWithoutResult(any());
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(drinkCardAccount));

        assertThrows(DrinkCardAccountSuspendedException.class, () -> service.execute(command(volunteerId)));

        verify(paymentGateway, never()).createHostedCheckout(any());
        verify(paymentRepository, never()).save(any());
        verify(paymentRepository, never()).findByIdempotencyKey(any());
    }

    private CreatePaymentCheckoutCommand command(VolunteerID volunteerId) {
        return new CreatePaymentCheckoutCommand(
                volunteerId.asString(),
                REDIRECT_URL
        );
    }

    private HostedCheckout hostedCheckout(Instant providerCreatedAt) {
        return new HostedCheckout(
                PaymentTestBuilder.DEFAULT_PROVIDER_CHECKOUT_ID,
                PaymentTestBuilder.DEFAULT_PROVIDER_CHECKOUT_URL,
                providerCreatedAt
        );
    }

    @SuppressWarnings("unchecked")
    private Answer<Object> executeTransactionCallback() {
        return invocation -> {
            TransactionCallback<Object> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        };
    }

    @SuppressWarnings("unchecked")
    private Answer<Void> executeWithoutResultCallback() {
        return invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        };
    }
}
