package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.EnableDrinkCardAccountRefillCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.EnableDrinkCardAccountRefillUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnableDrinkCardAccountRefillService implements EnableDrinkCardAccountRefillUseCase {

    private final DrinkCardAccountRepository drinkCardAccountRepository;

    public EnableDrinkCardAccountRefillService(DrinkCardAccountRepository drinkCardAccountRepository) {
        this.drinkCardAccountRepository = drinkCardAccountRepository;
    }

    @Transactional
    @Override
    public DrinkCardAccountSummaryResult execute(EnableDrinkCardAccountRefillCommand command) {
        DrinkCardAccount account = drinkCardAccountRepository.findByVolunteerId(VolunteerID.from(command.volunteerId()))
                .orElseThrow(() -> new DrinkCardAccountNotFoundException("DrinkCardAccount not found with id: " + command.volunteerId()));

        account.enableRefill();

        DrinkCardAccount saved = drinkCardAccountRepository.save(account);

        return DrinkCardAccountSummaryResult.from(saved);
    }
}
