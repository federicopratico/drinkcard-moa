package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

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
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CreatePaymentCheckoutService implements CreatePaymentCheckoutUseCase {

    final PaymentRepository paymentRepository;
    final PaymentGateway paymentGateway;
    final DrinkCardAccountRepository drinkCardAccountRepository;
    final TransactionTemplate transactionTemplate;
    final PaymentProperties paymentProperties;

    @Override
    public CreatePaymentCheckoutResult execute(CreatePaymentCheckoutCommand cmd) {

        Instant purchaseTimestamp = Instant.now();
        Instant expiresAt = purchaseTimestamp.plus(paymentProperties.getCheckoutExpiration());

        Payment existingPayment = findExistingPayment(cmd.idempotencyKey());

        if (existingPayment != null) {
            return toPaymentCheckoutResult(existingPayment);
        }

        validateDrinkCardAccountCanPurchase(cmd.volunteerId(), purchaseTimestamp);

        Card card = Card.newCard();

        Payment pendingPayment = Payment.pending(
                VolunteerID.from(cmd.volunteerId()),
                card.getPrice(),
                cmd.idempotencyKey(),
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

        validateDrinkCardAccountCanPurchase(cmd.volunteerId(), purchaseTimestamp);

        try {
            Payment savedPayment = savePaymentWithProviderCheckout(pendingPayment, hostedCheckout);
            return toPaymentCheckoutResult(savedPayment);
        } catch (DataIntegrityViolationException e) {
            Payment concurrentlySavedPayment = findExistingPayment(cmd.idempotencyKey());

            if (concurrentlySavedPayment != null)  {
                return toPaymentCheckoutResult(concurrentlySavedPayment);
            }

            throw  e;
        }
    }

    private Payment findExistingPayment(String idempotencyKey) {
        return transactionTemplate.execute(status -> {
            return paymentRepository.findByIdempotencyKey(idempotencyKey)
                    .orElse(null);
        });
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

            if (!account.isActive()) {
                throw new DrinkCardAccountSuspendedException("DrinkCardAccount is suspended.");
            }

            if (!account.canPurchaseCard(purchaseTimestamp)) {
                throw new PurchaseLimitExceededException("DrinkCardAccount has exceeded the purchase limit for today.");
            }
        });
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
