package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.ConfirmPaymentCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreatePaymentCheckoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConfirmPaymentResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreatePaymentCheckoutResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.CreatePaymentCheckoutRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.ConfirmPaymentResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.CreatePaymentCheckoutResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.PaymentSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentControllerMapperTest {

    private final PaymentControllerMapper mapper = new PaymentControllerMapper();

    @Test
    void toCommand_WhenCreatePaymentCheckoutRequest_ShouldMapRequestToCommand() {
        CreatePaymentCheckoutRequest request = new CreatePaymentCheckoutRequest();
        String redirectUrl = "http://localhost:3000/payment/success";

        CreatePaymentCheckoutCommand command = mapper.toCommand(request, "volunteer-123", redirectUrl);

        assertAll(
                () -> assertEquals("volunteer-123", command.volunteerId()),
                () -> assertEquals(redirectUrl, command.redirectUrl())
        );
    }

    @Test
    void toResponse_WhenCreatePaymentCheckoutResult_ShouldMapResultToResponse() {
        CreatePaymentCheckoutResult result = new CreatePaymentCheckoutResult(
                "payment-123",
                "https://checkout.sumup.com/checkout-123",
                "PENDING",
                BigDecimal.valueOf(10)
        );

        CreatePaymentCheckoutResponse response = mapper.toResponse(result);

        assertAll(
                () -> assertEquals("payment-123", response.paymentId()),
                () -> assertEquals("https://checkout.sumup.com/checkout-123", response.checkoutUrl()),
                () -> assertEquals("PENDING", response.status()),
                () -> assertEquals(BigDecimal.valueOf(10), response.amount())
        );
    }

    @Test
    void toCommand_WhenPaymentId_ShouldMapPaymentIdToConfirmPaymentCommand() {
        String paymentId = "payment-123";

        ConfirmPaymentCommand command = mapper.toCommand(paymentId);

        assertEquals(paymentId, command.paymentId());
    }

    @Test
    void toResponse_WhenConfirmPaymentResult_ShouldMapResultToResponse() {
        ConfirmPaymentResult result = new ConfirmPaymentResult(
                "payment-123",
                "SUCCESS",
                5,
                BigDecimal.valueOf(10)
        );

        ConfirmPaymentResponse response = mapper.toResponse(result);

        assertAll(
                () -> assertEquals("payment-123", response.paymentId()),
                () -> assertEquals("SUCCESS", response.status()),
                () -> assertEquals(5, response.credits()),
                () -> assertEquals(BigDecimal.valueOf(10), response.amount())
        );
    }

    @Test
    void toResponse_WhenPaymentSummaryResult_ShouldMapResultToResponse() {
        Instant paidAt = Instant.parse("2026-05-19T20:05:00Z");
        Instant createdAt = Instant.parse("2026-05-19T20:00:00Z");
        PaymentSummaryResult result = paymentSummaryResult(paidAt, createdAt);

        PaymentSummaryResponse response = mapper.toResponse(result);

        assertAll(
                () -> assertEquals(result.paymentId(), response.paymentId()),
                () -> assertEquals(result.volunteerId(), response.volunteerId()),
                () -> assertEquals(result.amount(), response.amount()),
                () -> assertEquals(result.status(), response.status()),
                () -> assertEquals(result.providerCheckoutId(), response.providerCheckoutId()),
                () -> assertEquals(result.providerCheckoutUrl(), response.providerCheckoutUrl()),
                () -> assertEquals(paidAt, response.paidAt()),
                () -> assertEquals(createdAt, response.createdAt())
        );
    }

    @Test
    void toResponse_WhenPagedPaymentSummaryResult_ShouldMapPageMetadataAndContent() {
        Instant paidAt = Instant.parse("2026-05-19T20:05:00Z");
        Instant createdAt = Instant.parse("2026-05-19T20:00:00Z");
        PaymentSummaryResult payment = paymentSummaryResult(paidAt, createdAt);

        PageResponse<PaymentSummaryResponse> response = mapper.toResponse(
                new PageResult<>(List.of(payment), 1, 10, 25, 3)
        );

        PaymentSummaryResponse paymentResponse = response.content().getFirst();

        assertAll(
                () -> assertEquals(1, response.page()),
                () -> assertEquals(10, response.size()),
                () -> assertEquals(25, response.totalElements()),
                () -> assertEquals(3, response.totalPages()),
                () -> assertEquals(payment.paymentId(), paymentResponse.paymentId()),
                () -> assertEquals(payment.volunteerId(), paymentResponse.volunteerId()),
                () -> assertEquals(payment.amount(), paymentResponse.amount()),
                () -> assertEquals(payment.status(), paymentResponse.status()),
                () -> assertEquals(payment.providerCheckoutId(), paymentResponse.providerCheckoutId()),
                () -> assertEquals(payment.providerCheckoutUrl(), paymentResponse.providerCheckoutUrl()),
                () -> assertEquals(payment.paidAt(), paymentResponse.paidAt()),
                () -> assertEquals(payment.createdAt(), paymentResponse.createdAt())
        );
    }

    private PaymentSummaryResult paymentSummaryResult(Instant paidAt, Instant createdAt) {
        return new PaymentSummaryResult(
                "payment-123",
                "volunteer-123",
                "Ada",
                "Lovelace",
                "ada@example.com",
                BigDecimal.valueOf(10),
                "SUCCESS",
                "checkout-id",
                "https://checkout.example.test",
                paidAt,
                createdAt
        );
    }
}
