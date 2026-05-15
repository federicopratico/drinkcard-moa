package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.ConfirmPaymentCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.ConfirmPaymentResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase.ConfirmPaymentUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.VolunteerRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.payment.PaymentGateway;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.payment.PaymentGatewayStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.exception.PaymentNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.PaymentID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Volunteer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ConfirmPaymentService implements ConfirmPaymentUseCase {

    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;
    private final VolunteerRepository volunteerRepository;

    public ConfirmPaymentService(PaymentGateway paymentGateway, PaymentRepository paymentRepository, EventPublisher eventPublisher, VolunteerRepository volunteerRepository) {
        this.paymentGateway = paymentGateway;
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
        this.volunteerRepository = volunteerRepository;
    }

    @Transactional
    @Override
    public ConfirmPaymentResult execute(ConfirmPaymentCommand cmd) {

        Payment payment = paymentRepository
                .findByPaymentId(PaymentID.from(cmd.paymentId()))
                .orElseThrow(() -> new PaymentNotFoundException("No payment found with id: " + cmd.paymentId()));

        Volunteer volunteer = volunteerRepository
                .findByVolunteerId(payment.getVolunteerId())
                .orElseThrow(() -> new PaymentNotFoundException("No volunteer found with id: " + payment.getVolunteerId()));

        if (payment.isFinalized()) {
            return toConfirmPaymentResult(payment, volunteer);
        }

        PaymentGatewayStatus providerStatus = paymentGateway.fetchCheckoutStatus(payment.getProviderCheckoutId());

        applyStatusChange(payment, volunteer, providerStatus);

        volunteerRepository.save(volunteer);
        paymentRepository.save(payment);

        volunteer.getDomainEvents().forEach(eventPublisher::publish);

        return toConfirmPaymentResult(payment, volunteer);
    }

    private void applyStatusChange(Payment payment, Volunteer volunteer, PaymentGatewayStatus providerStatus) {
        switch (providerStatus) {
            case PAID -> {
                payment.markAsSuccess();
                Card card = Card.newCard();
                volunteer.purchaseCard(card, Instant.now());
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

    private ConfirmPaymentResult toConfirmPaymentResult(Payment payment, Volunteer volunteer) {
        return new ConfirmPaymentResult(
                payment.getPaymentId().asString(),
                payment.getStatus().name(),
                volunteer.getCredits(),
                payment.getAmount()
        );
    }
}
