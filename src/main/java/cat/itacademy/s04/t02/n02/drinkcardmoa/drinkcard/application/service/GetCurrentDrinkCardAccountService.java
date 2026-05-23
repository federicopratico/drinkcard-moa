package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetCurrentDrinkCardAccountQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CurrentDrinkCardAccountResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.GetCurrentDrinkCardAccountUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Service;

@Service
public class GetCurrentDrinkCardAccountService implements GetCurrentDrinkCardAccountUseCase {

    private final DrinkCardAccountRepository drinkCardAccountRepository;

    public GetCurrentDrinkCardAccountService(DrinkCardAccountRepository drinkCardAccountRepository) {
        this.drinkCardAccountRepository = drinkCardAccountRepository;
    }

    @Override
    public CurrentDrinkCardAccountResult execute(GetCurrentDrinkCardAccountQuery query) {
        VolunteerID volunteerId = VolunteerID.from(query.userId());

        DrinkCardAccount account = drinkCardAccountRepository.findByVolunteerId(volunteerId)
                .orElseThrow(() -> new DrinkCardAccountNotFoundException("DrinkCardAccount not found with id: " + query.userId()));

        return CurrentDrinkCardAccountResult.from(account);
    }
}
