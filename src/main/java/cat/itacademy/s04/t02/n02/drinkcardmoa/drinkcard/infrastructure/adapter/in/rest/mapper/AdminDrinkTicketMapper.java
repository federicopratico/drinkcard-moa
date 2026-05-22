package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkTicketSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.DrinkTicketSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.PageResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminDrinkTicketMapper {

    public PageResponse<DrinkTicketSummaryResponse> toResponse(PageResult<DrinkTicketSummaryResult> result) {
        List<DrinkTicketSummaryResponse> content = result.content()
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

    private DrinkTicketSummaryResponse toResponse(DrinkTicketSummaryResult result) {
        return new DrinkTicketSummaryResponse(
                result.drinkTicketId(),
                result.volunteerId(),
                result.drinkType(),
                result.status(),
                result.createdAt(),
                result.expiresAt(),
                result.consumedAt(),
                result.consumedByStaffId()
        );
    }
}
