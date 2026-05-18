package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetDrinkCardAccountByVolunteerIdQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.GetDrinkCardAccountByVolunteerIdUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListDrinkCardAccountsUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.DrinkCardAccountSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper.AdminDrinkCardAccountMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/admin/drink-card-accounts")
@AllArgsConstructor
public class AdminDrinkCardAccountController {

    private final ListDrinkCardAccountsUseCase listDrinkCardAccountsUseCase;
    private final GetDrinkCardAccountByVolunteerIdUseCase getDrinkCardAccountByVolunteerIdUseCase;
    private final AdminDrinkCardAccountMapper mapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DrinkCardAccountSummaryResponse>> getDrinkCardAccounts() {
        List<DrinkCardAccountSummaryResponse> result = listDrinkCardAccountsUseCase.execute().stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{volunteerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DrinkCardAccountSummaryResponse> getDrinkCardAccountByVolunteerId(@PathVariable String volunteerId) {
        DrinkCardAccountSummaryResult result = getDrinkCardAccountByVolunteerIdUseCase.execute(
                new GetDrinkCardAccountByVolunteerIdQuery(volunteerId)
        );

        return ResponseEntity.ok(mapper.toResponse(result));
    }
}
