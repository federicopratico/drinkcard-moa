package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.ConsumeDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreateDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConsumeDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkTicketSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.ConsumeDrinkTicketRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.CreateDrinkTicketRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.ConsumeDrinkTicketResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.CreateDrinkTicketResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.DrinkTicketSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

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

    @Test
    void toResponse_WhenPagedDrinkTicketSummaryResult_ShouldMapPageMetadataAndContent() {
        Instant createdAt = Instant.parse("2026-05-19T20:00:00Z");
        Instant expiresAt = Instant.parse("2026-05-19T20:01:30Z");
        Instant consumedAt = Instant.parse("2026-05-19T20:01:00Z");
        DrinkTicketSummaryResult drinkTicket = drinkTicketSummaryResult(createdAt, expiresAt, consumedAt);

        PageResponse<DrinkTicketSummaryResponse> response = mapper.toResponse(
                new PageResult<>(List.of(drinkTicket), 1, 10, 25, 3)
        );

        DrinkTicketSummaryResponse drinkTicketResponse = response.content().getFirst();

        assertAll(
                () -> assertEquals(1, response.page()),
                () -> assertEquals(10, response.size()),
                () -> assertEquals(25, response.totalElements()),
                () -> assertEquals(3, response.totalPages()),
                () -> assertEquals(drinkTicket.drinkTicketId(), drinkTicketResponse.drinkTicketId()),
                () -> assertEquals(drinkTicket.volunteerId(), drinkTicketResponse.volunteerId()),
                () -> assertEquals(drinkTicket.drinkType(), drinkTicketResponse.drinkType()),
                () -> assertEquals(drinkTicket.status(), drinkTicketResponse.status()),
                () -> assertEquals(drinkTicket.createdAt(), drinkTicketResponse.createdAt()),
                () -> assertEquals(drinkTicket.expiresAt(), drinkTicketResponse.expiresAt()),
                () -> assertEquals(drinkTicket.consumedAt(), drinkTicketResponse.consumedAt()),
                () -> assertEquals(drinkTicket.consumedByStaffId(), drinkTicketResponse.consumedByStaffId())
        );
    }

    private DrinkTicketSummaryResult drinkTicketSummaryResult(
            Instant createdAt,
            Instant expiresAt,
            Instant consumedAt
    ) {
        return new DrinkTicketSummaryResult(
                "7aab22f8-60d3-4700-8ba6-b35e67dfacb6",
                "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1",
                "BEER",
                "CONSUMED",
                createdAt,
                expiresAt,
                consumedAt,
                "staff-123"
        );
    }
}
