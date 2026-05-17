package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreateDrinkCardAccountCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateDrinkCardAccountResult;

public interface CreateDrinkCardAccountUseCase {
    CreateDrinkCardAccountResult execute(CreateDrinkCardAccountCommand cmd);
}
