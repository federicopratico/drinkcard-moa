package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.DisableDrinkCardAccountRefillCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;

public interface DisableDrinkCardAccountRefillUseCase {
    DrinkCardAccountSummaryResult execute(DisableDrinkCardAccountRefillCommand command);
}
