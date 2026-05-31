package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetPaymentStatusQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentStatusResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.PaymentNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.testhelper.PaymentTestBuilder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPaymentStatusServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private GetPaymentStatusService service;

    @Test
    void execute_WhenPaymentBelongsToVolunteer_ReturnsPaymentStatus() {
        VolunteerID volunteerId = VolunteerID.generate();
        Payment payment = PaymentTestBuilder.aPayment()
                .withVolunteerId(volunteerId)
                .withStatus(PaymentStatus.SUCCESS)
                .build();

        when(paymentRepository.findByPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.of(payment));

        PaymentStatusResult result = service.execute(new GetPaymentStatusQuery(
                payment.getPaymentId().asString(),
                volunteerId.asString()
        ));

        assertAll(
                () -> assertEquals(payment.getPaymentId().asString(), result.paymentId()),
                () -> assertEquals(PaymentStatus.SUCCESS.name(), result.status()),
                () -> assertEquals(payment.getAmount(), result.amount())
        );
    }

    @Test
    void execute_WhenPaymentDoesNotExist_ThrowsPaymentNotFoundException() {
        PaymentID paymentId = PaymentID.generate();

        when(paymentRepository.findByPaymentId(paymentId))
                .thenReturn(Optional.empty());

        assertThrows(
                PaymentNotFoundException.class,
                () -> service.execute(new GetPaymentStatusQuery(
                        paymentId.asString(),
                        VolunteerID.generate().asString()
                ))
        );
    }

    @Test
    void execute_WhenPaymentBelongsToAnotherVolunteer_ThrowsPaymentNotFoundException() {
        VolunteerID paymentOwnerId = VolunteerID.generate();
        VolunteerID authenticatedVolunteerId = VolunteerID.generate();
        Payment payment = PaymentTestBuilder.aPayment()
                .withVolunteerId(paymentOwnerId)
                .build();

        when(paymentRepository.findByPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.of(payment));

        assertThrows(
                PaymentNotFoundException.class,
                () -> service.execute(new GetPaymentStatusQuery(
                        payment.getPaymentId().asString(),
                        authenticatedVolunteerId.asString()
                ))
        );
    }
}
