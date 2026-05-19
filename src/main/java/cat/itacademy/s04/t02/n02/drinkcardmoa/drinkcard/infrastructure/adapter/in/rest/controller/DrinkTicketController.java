package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetDrinkTicketStatusQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConsumeDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkTicketStatusResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ConsumeDrinkTicketUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.CreateDrinkTicketUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.GetDrinkTicketStatusUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.ConsumeDrinkTicketRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.CreateDrinkTicketRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.ConsumeDrinkTicketResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.CreateDrinkTicketResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.DrinkTicketStatusResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper.DrinkTicketControllerMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drink-tickets")
@AllArgsConstructor
public class DrinkTicketController {

    private final CreateDrinkTicketUseCase createDrinkTicketUseCase;
    private final ConsumeDrinkTicketUseCase consumeDrinkTicketUseCase;
    private final GetDrinkTicketStatusUseCase getDrinkTicketStatusUseCase;
    private final DrinkTicketControllerMapper mapper;

    @PostMapping
    public ResponseEntity<CreateDrinkTicketResponse> createDrinkTicket(@Valid @RequestBody CreateDrinkTicketRequest request) {
        CreateDrinkTicketResult result = createDrinkTicketUseCase.execute(mapper.toCommand(request));

        return ResponseEntity.status(201).body(mapper.toResponse(result));
    }


    @PostMapping("/{ticketId}/consume")
    public ResponseEntity<ConsumeDrinkTicketResponse> consumeDrinkTicket(
            @PathVariable String ticketId,
            @Valid @RequestBody ConsumeDrinkTicketRequest request) {
        ConsumeDrinkTicketResult result = consumeDrinkTicketUseCase.execute(mapper.toCommand(ticketId, request));

        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @GetMapping("/{ticketId}/status")
    public ResponseEntity<DrinkTicketStatusResponse> getTicketStatus(@PathVariable String ticketId) {
        GetDrinkTicketStatusQuery query = new GetDrinkTicketStatusQuery(ticketId);
        DrinkTicketStatusResult result = getDrinkTicketStatusUseCase.execute(query);

        return ResponseEntity.ok(mapper.toResponse(result));
    }
}
