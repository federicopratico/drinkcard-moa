package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreateDrinkCardAccountCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateDrinkCardAccountResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.CreateDrinkCardAccountUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccount;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateDrinkCardAccountService implements CreateDrinkCardAccountUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateDrinkCardAccountService.class);

    private final DrinkCardAccountRepository drinkCardAccountRepository;

    public CreateDrinkCardAccountService(DrinkCardAccountRepository drinkCardAccountRepository) {
        this.drinkCardAccountRepository = drinkCardAccountRepository;
    }

    @Transactional
    @Override
    public CreateDrinkCardAccountResult execute(CreateDrinkCardAccountCommand cmd) {
        log.info("Creating drink card account for volunteerId: {}", cmd.volunteerId());

        VolunteerID volunteerId = VolunteerID.from(cmd.volunteerId());

        if (drinkCardAccountRepository.existsByVolunteerId(volunteerId)) {
            log.warn("DrinkCardAccount already exists with volunteerId: {}", cmd.volunteerId());

            DrinkCardAccount existingDrinkCardAccount = drinkCardAccountRepository
                    .findByVolunteerId(volunteerId)
                    .orElseThrow();

            return CreateDrinkCardAccountResult.from(existingDrinkCardAccount);
        }

        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(volunteerId);

        DrinkCardAccount savedDrinkCardAccount = drinkCardAccountRepository.save(drinkCardAccount);

        log.info("Successfully created drink card account: {}", savedDrinkCardAccount.getVolunteerId().asString());

        return CreateDrinkCardAccountResult.from(savedDrinkCardAccount);
    }
}
