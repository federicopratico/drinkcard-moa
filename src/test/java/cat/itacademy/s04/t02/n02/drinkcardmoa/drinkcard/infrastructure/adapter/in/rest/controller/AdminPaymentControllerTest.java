package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.AddDrinkCardCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListPaymentsAdminQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.AddDrinkCardResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.AddDrinkCardUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListPaymentsAdminUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.AddDrinkCardRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.AddDrinkCardResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.PaymentSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper.AdminPaymentControllerMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPaymentControllerTest {

    @Mock
    private ListPaymentsAdminUseCase listPaymentsAdminUseCase;

    @Mock
    private AddDrinkCardUseCase addDrinkCardUseCase;

    private AdminPaymentController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminPaymentController(
                listPaymentsAdminUseCase,
                addDrinkCardUseCase,
                new AdminPaymentControllerMapper()
        );
    }

    @Test
    void listPayments_WhenPaymentsExist_ShouldReturnPagedPaymentResponse() {
        String volunteerId = "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1";
        Instant from = Instant.parse("2026-05-01T00:00:00Z");
        Instant to = Instant.parse("2026-05-19T23:59:59Z");
        Instant createdAt = Instant.parse("2026-05-19T20:00:00Z");

        PaymentSummaryResult payment = new PaymentSummaryResult(
                "7aab22f8-60d3-4700-8ba6-b35e67dfacb6",
                volunteerId,
                BigDecimal.valueOf(10),
                "SUCCESS",
                "checkout-id",
                "https://checkout.example.test",
                Instant.parse("2026-05-19T20:05:00Z"),
                createdAt
        );

        when(listPaymentsAdminUseCase.execute(new ListPaymentsAdminQuery(
                volunteerId,
                "SUCCESS",
                from,
                to,
                1,
                10,
                "createdAt,desc"
        ))).thenReturn(new PageResult<>(List.of(payment), 1, 10, 35, 4));

        ResponseEntity<PageResponse<PaymentSummaryResponse>> response = controller.listPayments(
                volunteerId,
                "SUCCESS",
                from,
                to,
                1,
                10,
                "createdAt,desc"
        );

        ArgumentCaptor<ListPaymentsAdminQuery> queryCaptor = ArgumentCaptor.forClass(ListPaymentsAdminQuery.class);
        verify(listPaymentsAdminUseCase).execute(queryCaptor.capture());

        PageResponse<PaymentSummaryResponse> body = response.getBody();
        PaymentSummaryResponse paymentResponse = body.content().getFirst();
        ListPaymentsAdminQuery query = queryCaptor.getValue();

        assertAll(
                () -> assertEquals(200, response.getStatusCode().value()),
                () -> assertNotNull(body),
                () -> assertEquals(1, body.page()),
                () -> assertEquals(10, body.size()),
                () -> assertEquals(35, body.totalElements()),
                () -> assertEquals(4, body.totalPages()),
                () -> assertEquals(payment.paymentId(), paymentResponse.paymentId()),
                () -> assertEquals(payment.volunteerId(), paymentResponse.volunteerId()),
                () -> assertEquals(payment.amount(), paymentResponse.amount()),
                () -> assertEquals(payment.status(), paymentResponse.status()),
                () -> assertEquals(payment.providerCheckoutId(), paymentResponse.providerCheckoutId()),
                () -> assertEquals(payment.providerCheckoutUrl(), paymentResponse.providerCheckoutUrl()),
                () -> assertEquals(payment.paidAt(), paymentResponse.paidAt()),
                () -> assertEquals(payment.createdAt(), paymentResponse.createdAt()),
                () -> assertEquals(volunteerId, query.volunteerId()),
                () -> assertEquals("SUCCESS", query.status()),
                () -> assertEquals(from, query.from()),
                () -> assertEquals(to, query.to()),
                () -> assertEquals(1, query.page()),
                () -> assertEquals(10, query.size()),
                () -> assertEquals("createdAt,desc", query.sort())
        );
    }

    @Test
    void listPayments_ShouldRequireAdminRole() throws NoSuchMethodException {
        Method method = AdminPaymentController.class.getMethod(
                "listPayments",
                String.class,
                String.class,
                Instant.class,
                Instant.class,
                int.class,
                int.class,
                String.class
        );

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertAll(
                () -> assertNotNull(preAuthorize),
                () -> assertEquals("hasRole('ADMIN')", preAuthorize.value())
        );
    }

    @Test
    void addDrinkCardManually_WhenUseCaseSucceeds_ShouldReturnAddDrinkCardResponse() {
        String volunteerId = "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1";
        AddDrinkCardResult result = new AddDrinkCardResult(
                volunteerId,
                5,
                BigDecimal.TEN
        );

        when(addDrinkCardUseCase.execute(new AddDrinkCardCommand(volunteerId))).thenReturn(result);

        ResponseEntity<AddDrinkCardResponse> response = controller.addDrinkCardManually(
                new AddDrinkCardRequest(volunteerId)
        );

        ArgumentCaptor<AddDrinkCardCommand> commandCaptor = ArgumentCaptor.forClass(AddDrinkCardCommand.class);
        verify(addDrinkCardUseCase).execute(commandCaptor.capture());

        AddDrinkCardResponse body = response.getBody();
        AddDrinkCardCommand command = commandCaptor.getValue();

        assertAll(
                () -> assertEquals(200, response.getStatusCode().value()),
                () -> assertNotNull(body),
                () -> assertEquals(volunteerId, body.volunteerId()),
                () -> assertEquals(5, body.credits()),
                () -> assertEquals(0, BigDecimal.TEN.compareTo(body.amount())),
                () -> assertEquals(volunteerId, command.volunteerId())
        );
    }

    @Test
    void addDrinkCardManually_ShouldRequireAdminRole() throws NoSuchMethodException {
        Method method = AdminPaymentController.class.getMethod(
                "addDrinkCardManually",
                AddDrinkCardRequest.class
        );

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertAll(
                () -> assertNotNull(preAuthorize),
                () -> assertEquals("hasRole('ADMIN')", preAuthorize.value())
        );
    }
}
