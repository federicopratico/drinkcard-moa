package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.ConfirmPaymentCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConfirmPaymentResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ConfirmPaymentUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGateway;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGatewayStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.PaymentNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ConfirmPaymentService implements ConfirmPaymentUseCase {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ConfirmPaymentService.class);

    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;
    private final DrinkCardAccountRepository drinkCardAccountRepository;

    public ConfirmPaymentService(PaymentGateway paymentGateway, PaymentRepository paymentRepository, EventPublisher eventPublisher, DrinkCardAccountRepository drinkCardAccountRepository) {
        this.paymentGateway = paymentGateway;
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
        this.drinkCardAccountRepository = drinkCardAccountRepository;
    }

    @Transactional
    @Override
    public ConfirmPaymentResult execute(ConfirmPaymentCommand cmd) {

        Payment existingPayment = paymentRepository
                .findByPaymentId(PaymentID.from(cmd.paymentId()))
                .orElseThrow(() -> new PaymentNotFoundException("No payment found with id: " + cmd.paymentId()));

        DrinkCardAccount account = drinkCardAccountRepository
                .findByVolunteerId(existingPayment.getVolunteerId())
                .orElseThrow(() -> new DrinkCardAccountNotFoundException("No drink card account found with id: " + existingPayment.getVolunteerId()));

        if (existingPayment.isFinalized()) {
            return toConfirmPaymentResult(existingPayment, account);
        }

        PaymentGatewayStatus providerStatus = paymentGateway.fetchCheckoutStatus(existingPayment.getProviderCheckoutId());

        switch (providerStatus) {
            case PAID -> {
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
                log.info("SumUp checkout is still pending: {}", existingPayment.getProviderCheckoutId());
            }

            case UNKNOWN -> {
                log.warn("Ignoring unsupported SumUp checkout status for checkout id: {}", existingPayment.getProviderCheckoutId());
            }
        }

        return toConfirmPaymentResult(existingPayment, account);
    }

    private ConfirmPaymentResult toConfirmPaymentResult(Payment payment, DrinkCardAccount drinkCardAccount) {
        return new ConfirmPaymentResult(
                payment.getPaymentId().asString(),
                payment.getStatus().name(),
                drinkCardAccount.getCredits(),
                payment.getAmount()
        );
    }
}
