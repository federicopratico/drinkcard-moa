package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentStatusResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.GetPaymentStatusUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetPaymentStatusQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.PaymentNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentID;
import org.springframework.stereotype.Service;

@Service
public class GetPaymentStatusService implements GetPaymentStatusUseCase {

    private final PaymentRepository paymentRepository;

    public GetPaymentStatusService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentStatusResult execute(GetPaymentStatusQuery query) {
        Payment payment = paymentRepository.findByPaymentId(PaymentID.from(query.paymentId()))
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with paymentId: " + query.paymentId()));

        if(!payment.getVolunteerId().asString().equals(query.volunteerId())) {
            throw new PaymentNotFoundException("Payment not found with paymentId: " + query.paymentId());
        }

        return PaymentStatusResult.from(payment);
    }
}
