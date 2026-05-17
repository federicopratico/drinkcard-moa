package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreateDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.CreateDrinkTicketUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.InsufficientCreditsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkType;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateDrinkTicketService implements CreateDrinkTicketUseCase {

    private final DrinkTicketRepository drinkTicketRepository;
    private final DrinkCardAccountRepository drinkCardAccountRepository;

    public CreateDrinkTicketService(DrinkTicketRepository drinkTicketRepository, DrinkCardAccountRepository drinkCardAccountRepository) {
        this.drinkTicketRepository = drinkTicketRepository;
        this.drinkCardAccountRepository = drinkCardAccountRepository;
    }

    @Transactional
    @Override
    public CreateDrinkTicketResult execute(CreateDrinkTicketCommand cmd) {
        DrinkCardAccount drinkCardAccount = drinkCardAccountRepository.findByVolunteerId(VolunteerID.from(cmd.volunteerId()))
                .orElseThrow(() -> new DrinkCardAccountNotFoundException("DrinkCardAccount not found with id: " + cmd.volunteerId()));

        if (!drinkCardAccount.canConsumeCredit())
            throw new InsufficientCreditsException("DrinkCardAccount has insufficient credits");

        DrinkTicket drinkTicket = DrinkTicket.pending(
                drinkCardAccount.getVolunteerId(),
                DrinkType.valueOf(cmd.drinkType().toUpperCase())
        );

        DrinkTicket savedDrinkTicket = drinkTicketRepository.save(drinkTicket);

        return CreateDrinkTicketResult.from(savedDrinkTicket);
    }
}
