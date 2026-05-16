package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.ConsumeDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.CreateDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.ConsumeDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.CreateDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.request.ConsumeDrinkTicketRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.request.CreateDrinkTicketRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.response.ConsumeDrinkTicketResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.response.CreateDrinkTicketResponse;
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
}
