package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;

import java.util.List;

public interface ListDrinkCardAccountsUseCase {
    List<DrinkCardAccountSummaryResult> execute();
}
