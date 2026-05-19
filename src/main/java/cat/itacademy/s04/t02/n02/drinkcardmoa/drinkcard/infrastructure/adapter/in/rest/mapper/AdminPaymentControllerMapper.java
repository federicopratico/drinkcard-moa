package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.AdminPaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.AdminPaymentSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.PageResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminPaymentControllerMapper {

    public PageResponse<AdminPaymentSummaryResponse> toResponse(PageResult<AdminPaymentSummaryResult> result) {
        List<AdminPaymentSummaryResponse> content = result.content()
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

    private AdminPaymentSummaryResponse toResponse(AdminPaymentSummaryResult result) {
        return new AdminPaymentSummaryResponse(
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
}
