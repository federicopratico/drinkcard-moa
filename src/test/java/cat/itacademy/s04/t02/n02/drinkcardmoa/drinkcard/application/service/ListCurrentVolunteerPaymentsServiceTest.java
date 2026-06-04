package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListCurrentVolunteerPaymentsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.PaymentSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.testhelper.PaymentTestBuilder;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCurrentVolunteerPaymentsServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private ListCurrentVolunteerPaymentsService service;

    @Test
    void execute_WhenNoPaginationProvided_ShouldSearchAuthenticatedVolunteerPaymentsWithDefaults() {
        VolunteerID volunteerId = VolunteerID.generate();
        Payment payment = PaymentTestBuilder.aPayment()
                .withVolunteerId(volunteerId)
                .withStatus(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.searchVolunteerPayments(any(PaymentSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(payment), 0, 20, 1, 1));

        PageResult<PaymentSummaryResult> result = service.execute(
                new ListCurrentVolunteerPaymentsQuery(volunteerId.asString(), -1, 0, null)
        );

        ArgumentCaptor<PaymentSearchCriteria> criteriaCaptor =
                ArgumentCaptor.forClass(PaymentSearchCriteria.class);
        verify(paymentRepository).searchVolunteerPayments(criteriaCaptor.capture());

        PaymentSearchCriteria criteria = criteriaCaptor.getValue();
        PaymentSummaryResult paymentResult = result.content().getFirst();

        assertAll(
                () -> assertEquals(volunteerId, criteria.volunteerId()),
                () -> assertNull(criteria.status()),
                () -> assertNull(criteria.from()),
                () -> assertNull(criteria.to()),
                () -> assertEquals(0, criteria.page()),
                () -> assertEquals(20, criteria.size()),
                () -> assertEquals("createdAt", criteria.sortBy()),
                () -> assertEquals("desc", criteria.sortDirection()),
                () -> assertEquals(1, result.content().size()),
                () -> assertEquals(payment.getPaymentId().asString(), paymentResult.paymentId()),
                () -> assertEquals(volunteerId.asString(), paymentResult.volunteerId()),
                () -> assertEquals(payment.getAmount(), paymentResult.amount()),
                () -> assertEquals(payment.getStatus().name(), paymentResult.status())
        );
    }

    @Test
    void execute_WhenPaginationProvided_ShouldPassParsedCriteriaToRepository() {
        VolunteerID volunteerId = VolunteerID.generate();

        when(paymentRepository.searchVolunteerPayments(any(PaymentSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(), 2, 10, 0, 0));

        service.execute(new ListCurrentVolunteerPaymentsQuery(
                volunteerId.asString(),
                2,
                10,
                "amount,asc"
        ));

        ArgumentCaptor<PaymentSearchCriteria> criteriaCaptor =
                ArgumentCaptor.forClass(PaymentSearchCriteria.class);
        verify(paymentRepository).searchVolunteerPayments(criteriaCaptor.capture());

        PaymentSearchCriteria criteria = criteriaCaptor.getValue();

        assertAll(
                () -> assertEquals(volunteerId, criteria.volunteerId()),
                () -> assertEquals(2, criteria.page()),
                () -> assertEquals(10, criteria.size()),
                () -> assertEquals("amount", criteria.sortBy()),
                () -> assertEquals("asc", criteria.sortDirection())
        );
    }

    @Test
    void execute_WhenSizeExceedsMaximum_ShouldCapPageSize() {
        VolunteerID volunteerId = VolunteerID.generate();

        when(paymentRepository.searchVolunteerPayments(any(PaymentSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(), 0, 100, 0, 0));

        service.execute(new ListCurrentVolunteerPaymentsQuery(
                volunteerId.asString(),
                0,
                150,
                "paidAt,desc"
        ));

        ArgumentCaptor<PaymentSearchCriteria> criteriaCaptor =
                ArgumentCaptor.forClass(PaymentSearchCriteria.class);
        verify(paymentRepository).searchVolunteerPayments(criteriaCaptor.capture());

        PaymentSearchCriteria criteria = criteriaCaptor.getValue();

        assertAll(
                () -> assertEquals(100, criteria.size()),
                () -> assertEquals("paidAt", criteria.sortBy()),
                () -> assertEquals("desc", criteria.sortDirection())
        );
    }

    @Test
    void execute_WhenSortFieldIsInvalid_ShouldThrowIllegalArgumentException() {
        VolunteerID volunteerId = VolunteerID.generate();

        ListCurrentVolunteerPaymentsQuery query = new ListCurrentVolunteerPaymentsQuery(
                volunteerId.asString(),
                0,
                20,
                "volunteerId,asc"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(query)
        );
    }
}
