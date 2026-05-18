package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetCurrentDrinkCardAccountQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CurrentDrinkCardAccountResult;

public interface GetCurrentDrinkCardAccountUseCase {
    CurrentDrinkCardAccountResult execute(GetCurrentDrinkCardAccountQuery query);
}
