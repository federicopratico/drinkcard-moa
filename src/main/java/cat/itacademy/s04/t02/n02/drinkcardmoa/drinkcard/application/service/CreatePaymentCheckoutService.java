package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.CheckoutAlreadyInProgressException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.config.PaymentProperties;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreatePaymentCheckoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreatePaymentCheckoutResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.CreatePaymentCheckoutUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.HostedCheckout;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.HostedCheckoutRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGateway;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountSuspendedException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.PurchaseLimitExceededException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.RefillDisabledException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.UserNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.TurnRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.exception.NoTurnTodayException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CreatePaymentCheckoutService implements CreatePaymentCheckoutUseCase {

    final PaymentRepository paymentRepository;
    final PaymentGateway paymentGateway;
    final DrinkCardAccountRepository drinkCardAccountRepository;
    final TransactionTemplate transactionTemplate;
    final PaymentProperties paymentProperties;
    private final LockRegistry lockRegistry;
    private final UserRepository userRepository;
    private final TurnRepository turnRepository;

    private static final ZoneId FESTIVAL_ZONE = ZoneId.of("Europe/Rome");

    @Override
    public CreatePaymentCheckoutResult execute(CreatePaymentCheckoutCommand cmd) {
        Instant purchaseTimestamp = Instant.now();
        var key = cmd.volunteerId() + OffsetDateTime.ofInstant(purchaseTimestamp, ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE);
        var lock = lockRegistry.obtain(key);

        if (!lock.tryLock()) {
           throw new CheckoutAlreadyInProgressException("Another purchase is being processed for this volunteer.");
        }

        try {
            validateDrinkCardAccountCanPurchase(cmd.volunteerId(), purchaseTimestamp);

            Instant expiresAt = purchaseTimestamp.plus(paymentProperties.getCheckoutExpiration());

            Payment existingPayment = findExistingPayment(key);

            if (existingPayment != null) {
                if (existingPayment.getExpiresAt().isBefore(purchaseTimestamp)) {
                    paymentRepository.delete(existingPayment);
                } else {
                    return toPaymentCheckoutResult(existingPayment);
                }
            }

            Card card = Card.newCard();

            Payment pendingPayment = Payment.pending(
                    VolunteerID.from(cmd.volunteerId()),
                    card.getPrice(),
                    key,
                    purchaseTimestamp,
                    expiresAt
            );
            HostedCheckout hostedCheckout = paymentGateway.createHostedCheckout(
                    new HostedCheckoutRequest(
                            pendingPayment.getPaymentId().asString(),
                            pendingPayment.getAmount(),
                            "EUR",
                            "Drink card - 5 credits",
                            cmd.redirectUrl(),
                            paymentProperties.getWebhookReturnUrl(),
                            pendingPayment.getExpiresAt()
                    )
            );
            Payment savedPayment = savePaymentWithProviderCheckout(pendingPayment, hostedCheckout);
            return toPaymentCheckoutResult(savedPayment);
        } catch (DataIntegrityViolationException e) {
            Payment concurrentlySavedPayment = findExistingPayment(key);

            if (concurrentlySavedPayment != null)  {
                return toPaymentCheckoutResult(concurrentlySavedPayment);
            }

            throw  e;
        } finally {
            lock.unlock();
        }
    }

    private Payment findExistingPayment(String key) {
        return transactionTemplate.execute(status -> paymentRepository.findByIdempotencyKey(key)
                .orElse(null));
    }

    private Payment savePaymentWithProviderCheckout(Payment payment, HostedCheckout hostedCheckout) {
        return transactionTemplate.execute(status -> {
            payment.attachProviderCheckoutId(hostedCheckout.providerCheckoutId());
            payment.attachProviderCheckoutUrl(hostedCheckout.checkoutUrl());
            payment.attachProviderCreatedAt(hostedCheckout.providerCreatedAt());

            return paymentRepository.save(payment);
        });
    }

    private void validateDrinkCardAccountCanPurchase(String volunteerId, Instant purchaseTimestamp) {
        transactionTemplate.executeWithoutResult(status -> {
            DrinkCardAccount account = drinkCardAccountRepository
                    .findByVolunteerId(VolunteerID.from(volunteerId))
                    .orElseThrow(() -> new DrinkCardAccountNotFoundException("DrinkCardAccount not found with id: " + volunteerId));

            if (!account.canRefill()) {
                throw new RefillDisabledException("Refill is disabled for this DrinkCardAccount.");
            }

            if (!account.canPurchaseCard(purchaseTimestamp)) {
                throw new PurchaseLimitExceededException("DrinkCardAccount has exceeded the purchase limit for today.");
            }

            validateVolunteerHasTurnToday(volunteerId, purchaseTimestamp);
        });
    }

    private void validateVolunteerHasTurnToday(String volunteerId, Instant purchaseTimestamp) {
        User user = userRepository.findById(VolunteerID.from(volunteerId))
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + volunteerId));

        LocalDate today = purchaseTimestamp.atZone(FESTIVAL_ZONE).toLocalDate();

        if (!turnRepository.existsByEmailAndDate(user.getEmail(), today)) {
            throw new NoTurnTodayException(
                    "Volunteer " + user.getEmail().asString() + " has no turn scheduled for " + today + " and cannot purchase a drink card."
            );
        }
    }

    private CreatePaymentCheckoutResult toPaymentCheckoutResult(Payment payment) {
        return new CreatePaymentCheckoutResult(
                payment.getPaymentId().asString(),
                payment.getProviderCheckoutUrl(),
                payment.getStatus().name(),
                payment.getAmount()
        );
    }
}
