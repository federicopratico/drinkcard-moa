package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.ConfirmPaymentCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreatePaymentCheckoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConfirmPaymentResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreatePaymentCheckoutResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.CreatePaymentCheckoutRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.ConfirmPaymentResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.CreatePaymentCheckoutResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentControllerMapperTest {

    private final PaymentControllerMapper mapper = new PaymentControllerMapper();

    @Test
    void toCommand_WhenCreatePaymentCheckoutRequest_ShouldMapRequestToCommand() {
        CreatePaymentCheckoutRequest request = new CreatePaymentCheckoutRequest(
                "volunteer-123",
                "idempotency-key-123"
        );
        String redirectUrl = "http://localhost:3000/payment/success";

        CreatePaymentCheckoutCommand command = mapper.toCommand(request, redirectUrl);

        assertAll(
                () -> assertEquals("volunteer-123", command.volunteerId()),
                () -> assertEquals(redirectUrl, command.redirectUrl()),
                () -> assertEquals("idempotency-key-123", command.idempotencyKey())
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
}
