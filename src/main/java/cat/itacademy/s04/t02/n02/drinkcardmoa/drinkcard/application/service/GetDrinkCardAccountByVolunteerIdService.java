package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetDrinkCardAccountByVolunteerIdQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.GetDrinkCardAccountByVolunteerIdUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Service;

@Service
public class GetDrinkCardAccountByVolunteerIdService implements GetDrinkCardAccountByVolunteerIdUseCase {

    private final DrinkCardAccountRepository drinkCardAccountRepository;

    public GetDrinkCardAccountByVolunteerIdService(DrinkCardAccountRepository drinkCardAccountRepository) {
        this.drinkCardAccountRepository = drinkCardAccountRepository;
    }

    @Override
    public DrinkCardAccountSummaryResult execute(GetDrinkCardAccountByVolunteerIdQuery query) {
        DrinkCardAccount account = drinkCardAccountRepository.findByVolunteerId(VolunteerID.from(query.volunteerId()))
                .orElseThrow(() -> new DrinkCardAccountNotFoundException("DrinkCardAccount not found with id: " + query.volunteerId()));

        return DrinkCardAccountSummaryResult.from(account);
    }
}
