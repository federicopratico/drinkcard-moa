package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class CreatePaymentCheckoutService implements CreatePaymentCheckoutUseCase {

    PaymentRepository paymentRepository;
    PaymentGateway paymentGateway;
    DrinkCardAccountRepository drinkCardAccountRepository;

    public CreatePaymentCheckoutService(PaymentRepository paymentRepository, PaymentGateway paymentGateway, DrinkCardAccountRepository drinkCardAccountRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.drinkCardAccountRepository = drinkCardAccountRepository;
    }

    @Transactional
    @Override
    public CreatePaymentCheckoutResult execute(CreatePaymentCheckoutCommand cmd) {

        Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(cmd.idempotencyKey());

        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();

            return toPaymentCheckoutResult(payment);
        }

        DrinkCardAccount drinkCardAccount = drinkCardAccountRepository
                .findByVolunteerId(VolunteerID.from(cmd.volunteerId()))
                .orElseThrow(() -> new DrinkCardAccountNotFoundException("DrinkCardAccount not found with id: " + cmd.volunteerId()));

        if (!drinkCardAccount.isActive()) {
            throw new DrinkCardAccountSuspendedException("DrinkCardAccount is suspended.");
        }

        if (!drinkCardAccount.canPurchaseCard(Instant.now())) {
            throw new PurchaseLimitExceededException("DrinkCardAccount has exceeded the purchase limit for today.");
        }

        Card card = Card.newCard();

        Payment payment = Payment.pending(
                VolunteerID.from(cmd.volunteerId()),
                card.getPrice(),
                cmd.idempotencyKey()
                );

        payment = paymentRepository.save(payment);

        HostedCheckout hostedCheckout = paymentGateway.createHostedCheckout(
                new HostedCheckoutRequest(
                        payment.getPaymentId().asString(),
                        payment.getAmount(),
                        "EUR",
                        "Drink card - 5 credits",
                        cmd.redirectUrl()
                )
        );

        payment.attachProviderCheckoutId(hostedCheckout.providerCheckoutId());
        payment.attachProviderCheckoutUrl(hostedCheckout.checkoutUrl());
        payment = paymentRepository.save(payment);

        return toPaymentCheckoutResult(payment);
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
