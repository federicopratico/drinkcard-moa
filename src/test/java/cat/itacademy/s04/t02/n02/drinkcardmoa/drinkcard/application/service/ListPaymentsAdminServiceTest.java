package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListPaymentsAdminQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.AdminPaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.PaymentSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPaymentsAdminServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private ListPaymentsAdminService service;

    @Test
    void execute_WhenNoFiltersProvided_ShouldReturnPagedPaymentsWithDefaults() {
        Payment payment = createPayment(PaymentStatus.PENDING);
        when(paymentRepository.searchAdminPayments(org.mockito.ArgumentMatchers.any(PaymentSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(payment), 0, 20, 1, 1));

        PageResult<AdminPaymentSummaryResult> result = service.execute(
                new ListPaymentsAdminQuery(null, null, null, null, -1, 0, null)
        );

        ArgumentCaptor<PaymentSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(PaymentSearchCriteria.class);
        verify(paymentRepository).searchAdminPayments(criteriaCaptor.capture());

        PaymentSearchCriteria criteria = criteriaCaptor.getValue();
        AdminPaymentSummaryResult paymentResult = result.content().getFirst();

        assertAll(
                () -> assertNull(criteria.volunteerId()),
                () -> assertNull(criteria.status()),
                () -> assertNull(criteria.from()),
                () -> assertNull(criteria.to()),
                () -> assertEquals(0, criteria.page()),
                () -> assertEquals(20, criteria.size()),
                () -> assertEquals("createdAt", criteria.sortBy()),
                () -> assertEquals("desc", criteria.sortDirection()),
                () -> assertEquals(1, result.content().size()),
                () -> assertEquals(payment.getPaymentId().asString(), paymentResult.paymentId()),
                () -> assertEquals(payment.getVolunteerId().asString(), paymentResult.volunteerId()),
                () -> assertEquals(payment.getStatus().name(), paymentResult.status())
        );
    }

    @Test
    void execute_WhenFiltersProvided_ShouldPassParsedCriteriaToRepository() {
        VolunteerID volunteerId = VolunteerID.generate();
        Instant from = Instant.parse("2026-05-01T00:00:00Z");
        Instant to = Instant.parse("2026-05-19T23:59:59Z");

        when(paymentRepository.searchAdminPayments(org.mockito.ArgumentMatchers.any(PaymentSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(), 2, 10, 0, 0));

        service.execute(new ListPaymentsAdminQuery(
                volunteerId.asString(),
                "success",
                from,
                to,
                2,
                10,
                "paidAt,asc"
        ));

        ArgumentCaptor<PaymentSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(PaymentSearchCriteria.class);
        verify(paymentRepository).searchAdminPayments(criteriaCaptor.capture());

        PaymentSearchCriteria criteria = criteriaCaptor.getValue();

        assertAll(
                () -> assertEquals(volunteerId, criteria.volunteerId()),
                () -> assertEquals(PaymentStatus.SUCCESS, criteria.status()),
                () -> assertEquals(from, criteria.from()),
                () -> assertEquals(to, criteria.to()),
                () -> assertEquals(2, criteria.page()),
                () -> assertEquals(10, criteria.size()),
                () -> assertEquals("paidAt", criteria.sortBy()),
                () -> assertEquals("asc", criteria.sortDirection())
        );
    }

    @Test
    void execute_WhenStatusIsInvalid_ShouldThrowIllegalArgumentException() {
        ListPaymentsAdminQuery query = new ListPaymentsAdminQuery(
                null,
                "UNKNOWN",
                null,
                null,
                0,
                20,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(query)
        );
    }

    private Payment createPayment(PaymentStatus status) {
        Instant createdAt = Instant.parse("2026-05-19T20:00:00Z");

        return Payment.rehydrate(
                PaymentID.generate(),
                VolunteerID.generate(),
                "idempotency-key",
                BigDecimal.valueOf(5),
                status,
                "checkout-id",
                "https://checkout.example.test",
                null,
                createdAt
        );
    }
}
