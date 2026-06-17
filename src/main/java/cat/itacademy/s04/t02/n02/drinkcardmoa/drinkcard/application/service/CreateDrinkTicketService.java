package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.ActiveDrinkTicketAlreadyExistsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreateDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.CreateDrinkTicketUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountSuspendedException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.InsufficientCreditsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkType;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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
        VolunteerID volunteerId = VolunteerID.from(cmd.volunteerId());
        Instant now = Instant.now();

        DrinkCardAccount drinkCardAccount = drinkCardAccountRepository.findByVolunteerId(volunteerId)
                .orElseThrow(() -> new DrinkCardAccountNotFoundException("DrinkCardAccount not found with id: " + cmd.volunteerId()));

        validateDrinkTicketCanBeCreated(drinkCardAccount, now);

        DrinkTicket drinkTicket = DrinkTicket.pending(
                drinkCardAccount.getVolunteerId(),
                DrinkType.valueOf(cmd.drinkType().toUpperCase()),
                now
        );

        DrinkTicket savedDrinkTicket = drinkTicketRepository.save(drinkTicket);

        return CreateDrinkTicketResult.from(savedDrinkTicket);
    }

    private void validateDrinkTicketCanBeCreated(DrinkCardAccount drinkCardAccount, Instant now) {
        if (!drinkCardAccount.canCreateTicket()) {
            throw new DrinkCardAccountSuspendedException("DrinkCardAccount is suspended.");
        }

        if (!drinkCardAccount.canConsumeCredit()) {
            throw new InsufficientCreditsException("DrinkCardAccount has insufficient credits");
        }

        if (drinkTicketRepository.existsActivePendingByVolunteerId(drinkCardAccount.getVolunteerId(), now)) {
            throw new ActiveDrinkTicketAlreadyExistsException(
                    "Volunteer already has an active pending drink ticket."
            );
        }
    }
}
