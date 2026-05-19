package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetDrinkTicketStatusQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkTicketStatusResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.GetDrinkTicketStatusUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkTicketNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicketID;
import org.springframework.stereotype.Service;

@Service
public class GetDrinkTicketStatusService implements GetDrinkTicketStatusUseCase {

    private final DrinkTicketRepository drinkTicketRepository;

    public GetDrinkTicketStatusService(DrinkTicketRepository drinkTicketRepository) {
        this.drinkTicketRepository = drinkTicketRepository;
    }

    @Override
    public DrinkTicketStatusResult execute(GetDrinkTicketStatusQuery query) {

        DrinkTicketID drinkTicketId = DrinkTicketID.from(query.ticketId());

        DrinkTicket drinkTicket = drinkTicketRepository.findByDrinkTicketId(drinkTicketId)
                .orElseThrow(() -> new DrinkTicketNotFoundException("Drink ticket not found with id: " + drinkTicketId.asString()));

        return DrinkTicketStatusResult.from(drinkTicket);
    }
}
