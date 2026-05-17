package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.ConsumeDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.CreateDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.ConsumeDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.CreateDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.request.ConsumeDrinkTicketRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.request.CreateDrinkTicketRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.response.ConsumeDrinkTicketResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.dto.response.CreateDrinkTicketResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DrinkTicketControllerMapperTest {

    private final DrinkTicketControllerMapper mapper = new DrinkTicketControllerMapper();

    @Test
    void toCommand_ShouldMapCreateDrinkTicketRequestToCommand() {
        CreateDrinkTicketRequest request = new CreateDrinkTicketRequest(
                "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1",
                "BEER"
        );

        CreateDrinkTicketCommand command = mapper.toCommand(request);

        assertAll(
                () -> assertEquals(request.volunteerId(), command.volunteerId()),
                () -> assertEquals(request.drinkType(), command.drinkType())
        );
    }

    @Test
    void toCommand_ShouldMapConsumeDrinkTicketRequestToCommand() {
        String ticketId = "7aab22f8-60d3-4700-8ba6-b35e67dfacb6";
        ConsumeDrinkTicketRequest request = new ConsumeDrinkTicketRequest(
                "8799df50-d517-4693-9e46-51b537c305a2"
        );

        ConsumeDrinkTicketCommand command = mapper.toCommand(ticketId, request);

        assertAll(
                () -> assertEquals(ticketId, command.ticketId()),
                () -> assertEquals(request.consumedByStaffId(), command.consumedByStaffId())
        );
    }

    @Test
    void toResponse_ShouldMapCreateDrinkTicketResultToResponse() {
        Instant expiresAt = Instant.parse("2026-05-16T21:30:00Z");
        CreateDrinkTicketResult result = new CreateDrinkTicketResult(
                "7aab22f8-60d3-4700-8ba6-b35e67dfacb6",
                "BEER",
                "PENDING",
                expiresAt
        );

        CreateDrinkTicketResponse response = mapper.toResponse(result);

        assertAll(
                () -> assertEquals(result.ticketId(), response.ticketId()),
                () -> assertEquals(result.drinkType(), response.drinkType()),
                () -> assertEquals(result.status(), response.status()),
                () -> assertEquals(result.expiresAt(), response.expiresAt())
        );
    }

    @Test
    void toResponse_ShouldMapConsumeDrinkTicketResultToResponse() {
        ConsumeDrinkTicketResult result = new ConsumeDrinkTicketResult(
                "7aab22f8-60d3-4700-8ba6-b35e67dfacb6",
                "CONSUMED",
                "BEER",
                4
        );

        ConsumeDrinkTicketResponse response = mapper.toResponse(result);

        assertAll(
                () -> assertEquals(result.ticketId(), response.ticketId()),
                () -> assertEquals(result.status(), response.status()),
                () -> assertEquals(result.drinkType(), response.drinkType()),
                () -> assertEquals(result.remainingCredits(), response.remainingCredits())
        );
    }
}
