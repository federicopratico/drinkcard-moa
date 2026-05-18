package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CurrentDrinkCardAccountResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.GetCurrentDrinkCardAccountUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.CurrentDrinkCardAccountResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper.DrinkCardAccountControllerMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drink-card-accounts")
@AllArgsConstructor
public class DrinkCardAccountController {

    private final GetCurrentDrinkCardAccountUseCase getCurrentDrinkCardAccountUseCase;
    private final DrinkCardAccountControllerMapper mapper;

    @GetMapping("/me")
    public ResponseEntity<CurrentDrinkCardAccountResponse> getCurrentDrinkCardAccount(Authentication authentication) {
        CurrentDrinkCardAccountResult result = getCurrentDrinkCardAccountUseCase.execute(
                mapper.toQuery(authentication.getName())
        );

        return ResponseEntity.ok(mapper.toResponse(result));
    }
}
