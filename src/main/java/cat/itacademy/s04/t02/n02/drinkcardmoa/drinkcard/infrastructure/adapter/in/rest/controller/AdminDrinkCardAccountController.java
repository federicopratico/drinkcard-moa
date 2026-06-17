package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.DisableDrinkCardAccountRefillCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.EnableDrinkCardAccountRefillCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListDrinkCardAccountsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetDrinkCardAccountByVolunteerIdQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.DisableDrinkCardAccountRefillUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.EnableDrinkCardAccountRefillUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.GetDrinkCardAccountByVolunteerIdUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListDrinkCardAccountsUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.DrinkCardAccountSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper.AdminDrinkCardAccountMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/v1/admin/drink-card-accounts")
@AllArgsConstructor
public class AdminDrinkCardAccountController {

    private final ListDrinkCardAccountsUseCase listDrinkCardAccountsUseCase;
    private final GetDrinkCardAccountByVolunteerIdUseCase getDrinkCardAccountByVolunteerIdUseCase;
    private final DisableDrinkCardAccountRefillUseCase disableDrinkCardAccountRefillUseCase;
    private final EnableDrinkCardAccountRefillUseCase enableDrinkCardAccountRefillUseCase;
    private final AdminDrinkCardAccountMapper mapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<DrinkCardAccountSummaryResponse>> getDrinkCardAccounts(
            @RequestParam(required = false) String volunteerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "volunteerId,asc") String sort
    ) {
        PageResult<DrinkCardAccountSummaryResult> result = listDrinkCardAccountsUseCase.execute(
                new ListDrinkCardAccountsQuery(
                        volunteerId,
                        status,
                        page,
                        size,
                        sort
                )
        );

        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @GetMapping("/{volunteerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DrinkCardAccountSummaryResponse> getDrinkCardAccountByVolunteerId(@PathVariable String volunteerId) {
        DrinkCardAccountSummaryResult result = getDrinkCardAccountByVolunteerIdUseCase.execute(
                new GetDrinkCardAccountByVolunteerIdQuery(volunteerId)
        );

        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @PostMapping("/{volunteerId}/disable-refill")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DrinkCardAccountSummaryResponse> disableDrinkCardAccountRefill(@PathVariable String volunteerId) {
        DrinkCardAccountSummaryResult result = disableDrinkCardAccountRefillUseCase.execute(
                new DisableDrinkCardAccountRefillCommand(volunteerId)
        );

        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @PostMapping("/{volunteerId}/enable-refill")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DrinkCardAccountSummaryResponse> enableDrinkCardAccountRefill(@PathVariable String volunteerId) {
        DrinkCardAccountSummaryResult result = enableDrinkCardAccountRefillUseCase.execute(
                new EnableDrinkCardAccountRefillCommand(volunteerId)
        );

        return ResponseEntity.ok(mapper.toResponse(result));
    }
}
