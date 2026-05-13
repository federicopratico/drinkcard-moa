package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.ConfirmPaymentCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.CreatePaymentCheckoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.ConfirmPaymentResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.CreatePaymentCheckoutResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.request.CreatePaymentCheckoutRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.response.ConfirmPaymentResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.response.CreatePaymentCheckoutResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentControllerMapper {

    public CreatePaymentCheckoutCommand toCommand(CreatePaymentCheckoutRequest request, String redirectUrl) {
        return new CreatePaymentCheckoutCommand(
                request.volunteerId(),
                redirectUrl,
                request.idempotencyKey()
        );
    }

    public CreatePaymentCheckoutResponse toResponse(CreatePaymentCheckoutResult result) {
        return new CreatePaymentCheckoutResponse(
                result.paymentId(),
                result.checkoutUrl(),
                result.status(),
                result.amount()
        );
    }

    public ConfirmPaymentCommand toCommand(String paymentId) {
        return new ConfirmPaymentCommand(paymentId);
    }

    public ConfirmPaymentResponse toResponse(ConfirmPaymentResult result) {
        return new ConfirmPaymentResponse(
                result.paymentId(),
                result.status(),
                result.credits(),
                result.amount()
        );
    }
}
