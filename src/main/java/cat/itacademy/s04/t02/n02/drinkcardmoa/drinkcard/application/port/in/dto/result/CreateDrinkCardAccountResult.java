package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccount;

public record CreateDrinkCardAccountResult(
        String volunteerID,
        int credits
)
{
    public static CreateDrinkCardAccountResult from(DrinkCardAccount drinkCardAccount) {
        return new CreateDrinkCardAccountResult(
                drinkCardAccount.getVolunteerId().asString(),
                drinkCardAccount.getCredits()
        );
    }
}
