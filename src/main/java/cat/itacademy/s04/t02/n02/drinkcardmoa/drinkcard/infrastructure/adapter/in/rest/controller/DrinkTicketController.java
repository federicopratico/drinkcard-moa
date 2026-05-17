package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConsumeDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ConsumeDrinkTicketUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.CreateDrinkTicketUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.ConsumeDrinkTicketRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.CreateDrinkTicketRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.ConsumeDrinkTicketResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.CreateDrinkTicketResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper.DrinkTicketControllerMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drink-tickets")
public class DrinkTicketController {

    private final CreateDrinkTicketUseCase createDrinkTicketUseCase;
    private final ConsumeDrinkTicketUseCase consumeDrinkTicketUseCase;
    private final DrinkTicketControllerMapper mapper;

    public DrinkTicketController(
            CreateDrinkTicketUseCase createDrinkTicketUseCase,
            ConsumeDrinkTicketUseCase consumeDrinkTicketUseCase,
            DrinkTicketControllerMapper mapper) {
        this.createDrinkTicketUseCase = createDrinkTicketUseCase;
        this.consumeDrinkTicketUseCase = consumeDrinkTicketUseCase;
        this.mapper = mapper;
    }

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
}
