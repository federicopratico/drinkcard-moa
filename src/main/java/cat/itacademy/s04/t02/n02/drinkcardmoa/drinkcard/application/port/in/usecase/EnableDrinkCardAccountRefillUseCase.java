package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.EnableDrinkCardAccountRefillCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;

public interface EnableDrinkCardAccountRefillUseCase {
    DrinkCardAccountSummaryResult execute(EnableDrinkCardAccountRefillCommand command);
}
