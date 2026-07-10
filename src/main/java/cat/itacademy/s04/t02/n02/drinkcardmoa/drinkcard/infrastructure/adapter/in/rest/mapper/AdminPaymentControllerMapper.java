package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.AddDrinkCardResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.AddDrinkCardResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.PaymentSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminPaymentControllerMapper {

    public PageResponse<PaymentSummaryResponse> toResponse(PageResult<PaymentSummaryResult> result) {
        List<PaymentSummaryResponse> content = result.content()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }

    private PaymentSummaryResponse toResponse(PaymentSummaryResult result) {
        return new PaymentSummaryResponse(
                result.paymentId(),
                result.volunteerId(),
                result.amount(),
                result.status(),
                result.providerCheckoutId(),
                result.providerCheckoutUrl(),
                result.paidAt(),
                result.createdAt()
        );
    }

    public AddDrinkCardResponse toResponse(AddDrinkCardResult result) {
        return new AddDrinkCardResponse(
                result.volunteerId(),
                result.credits(),
                result.amount()
        );
    }
}
