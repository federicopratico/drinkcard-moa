package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListDrinkCardAccountsUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListDrinkCardAccountsService implements ListDrinkCardAccountsUseCase {

    private final DrinkCardAccountRepository drinkCardAccountRepository;

    public ListDrinkCardAccountsService(DrinkCardAccountRepository drinkCardAccountRepository) {
        this.drinkCardAccountRepository = drinkCardAccountRepository;
    }

    @Override
    public List<DrinkCardAccountSummaryResult> execute() {
        return drinkCardAccountRepository.findAll().stream()
                .map(DrinkCardAccountSummaryResult::from)
                .toList();
    }
}
