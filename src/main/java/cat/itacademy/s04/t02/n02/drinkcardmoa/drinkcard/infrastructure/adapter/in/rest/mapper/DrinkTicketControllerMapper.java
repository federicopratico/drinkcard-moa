package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.ConsumeDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreateDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConsumeDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkTicketStatusResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkTicketSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.ConsumeDrinkTicketRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.CreateDrinkTicketRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.ConsumeDrinkTicketResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.CreateDrinkTicketResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.DrinkTicketStatusResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.DrinkTicketSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import org.springframework.stereotype.Component;

@Component
public class DrinkTicketControllerMapper {

    public CreateDrinkTicketCommand toCommand(CreateDrinkTicketRequest request) {
        return new CreateDrinkTicketCommand(
                request.volunteerId(),
                request.drinkType()
        );
    }

    public ConsumeDrinkTicketCommand toCommand(String ticketId, ConsumeDrinkTicketRequest request) {
        return new ConsumeDrinkTicketCommand(
                ticketId,
                request.consumedByStaffId()
        );
    }

    public CreateDrinkTicketResponse toResponse(CreateDrinkTicketResult result) {
        return new CreateDrinkTicketResponse(
                result.ticketId(),
                result.drinkType(),
                result.status(),
                result.expiresAt()
        );
    }

    public ConsumeDrinkTicketResponse toResponse(ConsumeDrinkTicketResult result) {
        return new ConsumeDrinkTicketResponse(
                result.ticketId(),
                result.status(),
                result.drinkType(),
                result.remainingCredits()
        );
    }

    public DrinkTicketStatusResponse toResponse(DrinkTicketStatusResult result) {
        return new DrinkTicketStatusResponse(
                result.ticketId(),
                result.status(),
                result.drinkType(),
                result.expiresAt(),
                result.consumedAt()
        );
    }

    public PageResponse<DrinkTicketSummaryResponse> toResponse(PageResult<DrinkTicketSummaryResult> result) {
        return new PageResponse<>(
                result.content().stream().map(this::toResponse).toList(),
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
