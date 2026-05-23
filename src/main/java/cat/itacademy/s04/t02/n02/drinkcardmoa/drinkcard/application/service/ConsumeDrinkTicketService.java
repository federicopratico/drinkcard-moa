package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.ConsumeDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConsumeDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ConsumeDrinkTicketUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkTicketNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkTicketID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ConsumeDrinkTicketService implements ConsumeDrinkTicketUseCase {

    private final DrinkTicketRepository drinkTicketRepository;
    private final DrinkCardAccountRepository drinkCardAccountRepository;

    public ConsumeDrinkTicketService(DrinkTicketRepository drinkTicketRepository, DrinkCardAccountRepository drinkCardAccountRepository) {
        this.drinkTicketRepository = drinkTicketRepository;
        this.drinkCardAccountRepository = drinkCardAccountRepository;
    }

    @Transactional
    @Override
    public ConsumeDrinkTicketResult execute(ConsumeDrinkTicketCommand cmd) {
        DrinkTicket drinkTicket = drinkTicketRepository.findByDrinkTicketId(DrinkTicketID.from(cmd.ticketId()))
                .orElseThrow(() -> new DrinkTicketNotFoundException("Drink ticket not found."));

        DrinkCardAccount drinkCardAccount = drinkCardAccountRepository.findByVolunteerId(drinkTicket.getVolunteerId())
                .orElseThrow(() -> new DrinkCardAccountNotFoundException(
                        "DrinkCardAccount not found with id: " + drinkTicket.getVolunteerId().asString()));

        drinkCardAccount.consumeCredit();
        drinkTicket.consume(cmd.consumedByStaffId(), Instant.now());

        DrinkCardAccount savedDrinkCardAccount = drinkCardAccountRepository.save(drinkCardAccount);
        DrinkTicket savedDrinkTicket = drinkTicketRepository.save(drinkTicket);

        return toConsumeDrinkTicketResult(savedDrinkTicket, savedDrinkCardAccount);
    }

    private ConsumeDrinkTicketResult toConsumeDrinkTicketResult(DrinkTicket drinkTicket, DrinkCardAccount drinkCardAccount) {
        return new ConsumeDrinkTicketResult(
                drinkTicket.getDrinkTicketId().asString(),
                drinkTicket.getStatus().toString(),
                drinkTicket.getDrinkType().toString(),
                drinkCardAccount.getCredits()
        );
    }
}
