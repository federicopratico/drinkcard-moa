package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.ProcessPaymentWebhookCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ProcessPaymentWebhookUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGateway;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGatewayStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class ProcessPaymentWebhookService implements ProcessPaymentWebhookUseCase {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ProcessPaymentWebhookService.class);

    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final DrinkCardAccountRepository drinkCardAccountRepository;
    private final EventPublisher eventPublisher;

    public ProcessPaymentWebhookService(PaymentGateway paymentGateway, PaymentRepository paymentRepository, DrinkCardAccountRepository drinkCardAccountRepository, EventPublisher eventPublisher) {
        this.paymentGateway = paymentGateway;
        this.paymentRepository = paymentRepository;
        this.drinkCardAccountRepository = drinkCardAccountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @Override
    public void execute(ProcessPaymentWebhookCommand cmd) {

        Optional<Payment> payment = paymentRepository.findByProviderCheckoutId(cmd.providerCheckoutId());

        if(payment.isEmpty()) {
            log.warn("Ignoring SumUp webhook for unknown checkout id: {}", cmd.providerCheckoutId());
            return;
        }

        Payment existingPayment = payment.get();
        if (existingPayment.isFinalized()) {
            return;
        }

        PaymentGatewayStatus providerStatus = paymentGateway.fetchCheckoutStatus(cmd.providerCheckoutId());

        switch (providerStatus) {
            case PAID -> {
                DrinkCardAccount account = drinkCardAccountRepository.findByVolunteerId(existingPayment.getVolunteerId())
                        .orElseThrow(() -> new DrinkCardAccountNotFoundException(
                                "DrinkCardAccount not found with id: " + existingPayment.getVolunteerId()
                        ));

                existingPayment.markAsSuccess();
                account.purchaseCard(Card.newCard(), Instant.now());

                drinkCardAccountRepository.save(account);
                paymentRepository.save(existingPayment);

                account.getDomainEvents().forEach(eventPublisher::publish);
            }

            case FAILED -> {
                existingPayment.markAsFailed();
                paymentRepository.save(existingPayment);
            }

            case EXPIRED -> {
                existingPayment.markAsExpired();
                paymentRepository.save(existingPayment);
            }

            case PENDING -> {
                log.info("SumUp checkout is still pending: {}", cmd.providerCheckoutId());
            }

            case UNKNOWN -> {
                log.warn("Ignoring unsupported SumUp checkout status for checkout id: {}", cmd.providerCheckoutId());
            }
        }

        log.info("Payment status synchronized for checkout id: {}", cmd.providerCheckoutId());
    }
}
