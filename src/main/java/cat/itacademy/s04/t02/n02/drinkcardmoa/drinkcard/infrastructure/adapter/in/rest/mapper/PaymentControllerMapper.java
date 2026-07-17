package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.ConfirmPaymentCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreatePaymentCheckoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConfirmPaymentResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreatePaymentCheckoutResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentStatusResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.CreatePaymentCheckoutRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.ConfirmPaymentResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.CreatePaymentCheckoutResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.PaymentStatusResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.PaymentSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentControllerMapper {

    public CreatePaymentCheckoutCommand toCommand(CreatePaymentCheckoutRequest request, String volunteerId, String redirectUrl) {
        return new CreatePaymentCheckoutCommand(
                volunteerId,
                redirectUrl
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

    public PaymentSummaryResponse toResponse(PaymentSummaryResult result) {
        return new PaymentSummaryResponse(
                result.paymentId(),
                new PaymentSummaryResponse.VolunteerInfo(
                        result.volunteerId(),
                        result.volunteerFirstName(),
                        result.volunteerLastName(),
                        result.volunteerEmail()
                ),
                result.volunteerId(),
                result.amount(),
                result.status(),
                result.providerCheckoutId(),
                result.providerCheckoutUrl(),
                result.paidAt(),
                result.createdAt()
        );
    }

    public PageResponse<PaymentSummaryResponse> toResponse(PageResult<PaymentSummaryResult> result) {
        return new PageResponse<>(
                result.content().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }

    public PaymentStatusResponse toResponse(PaymentStatusResult result) {
        return new PaymentStatusResponse(
                result.paymentId(),
                result.status(),
                result.amount()
        );
    }
}
