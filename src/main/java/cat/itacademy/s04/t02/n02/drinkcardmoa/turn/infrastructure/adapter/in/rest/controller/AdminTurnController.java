package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.command.DeleteTurnCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.query.ListTurnsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.result.AddTurnResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.result.TurnSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.usecase.AddTurnUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.usecase.DeleteTurnUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.usecase.ListTurnsUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.in.rest.dto.request.AddTurnRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.in.rest.dto.response.TurnResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.in.rest.dto.response.TurnSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.in.rest.mapper.TurnControllerMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/turns")
@AllArgsConstructor
public class AdminTurnController {

    private final AddTurnUseCase addTurnUseCase;
    private final DeleteTurnUseCase deleteTurnUseCase;
    private final ListTurnsUseCase listTurnsUseCase;
    private final TurnControllerMapper mapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TurnResponse> addTurn(@Valid @RequestBody AddTurnRequest request) {
        AddTurnResult result = addTurnUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<TurnSummaryResponse>> listTurns(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResult<TurnSummaryResult> result = listTurnsUseCase.execute(
                new ListTurnsQuery(email, date, page, size)
        );
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @DeleteMapping("/{turnId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTurn(@PathVariable String turnId) {
        deleteTurnUseCase.execute(new DeleteTurnCommand(turnId));
        return ResponseEntity.noContent().build();
    }
}
