package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.ConfirmPaymentCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConfirmPaymentResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ConfirmPaymentUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGateway;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGatewayStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.PaymentNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ConfirmPaymentService implements ConfirmPaymentUseCase {

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

        Payment payment = paymentRepository
                .findByPaymentId(PaymentID.from(cmd.paymentId()))
                .orElseThrow(() -> new PaymentNotFoundException("No payment found with id: " + cmd.paymentId()));

        DrinkCardAccount drinkCardAccount = drinkCardAccountRepository
                .findByVolunteerId(payment.getVolunteerId())
                .orElseThrow(() -> new PaymentNotFoundException("No drink card account found with id: " + payment.getVolunteerId()));

        if (payment.isFinalized()) {
            return toConfirmPaymentResult(payment, drinkCardAccount);
        }

        PaymentGatewayStatus providerStatus = paymentGateway.fetchCheckoutStatus(payment.getProviderCheckoutId());

        applyStatusChange(payment, drinkCardAccount, providerStatus);

        drinkCardAccountRepository.save(drinkCardAccount);
        paymentRepository.save(payment);

        drinkCardAccount.getDomainEvents().forEach(eventPublisher::publish);

        return toConfirmPaymentResult(payment, drinkCardAccount);
    }

    private void applyStatusChange(Payment payment, DrinkCardAccount drinkCardAccount, PaymentGatewayStatus providerStatus) {
        switch (providerStatus) {
            case PAID -> {
                payment.markAsSuccess();
                Card card = Card.newCard();
                drinkCardAccount.purchaseCard(card, Instant.now());
            }

            case FAILED -> {
                payment.markAsFailed();
            }

            case EXPIRED -> {
                payment.markAsExpired();
            }

            default -> {}
        }
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
