package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.CreateDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.CreateDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase.CreateDrinkTickerUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.VolunteerRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.exception.InsufficientCreditsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.exception.VolunteerNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.DrinkType;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Volunteer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateDrinkTicketService implements CreateDrinkTickerUseCase {

    private final DrinkTicketRepository drinkTicketRepository;
    private final VolunteerRepository volunteerRepository;

    public CreateDrinkTicketService(DrinkTicketRepository drinkTicketRepository, VolunteerRepository volunteerRepository) {
        this.drinkTicketRepository = drinkTicketRepository;
        this.volunteerRepository = volunteerRepository;
    }

    @Transactional
    @Override
    public CreateDrinkTicketResult execute(CreateDrinkTicketCommand cmd) {
        Volunteer volunteer = volunteerRepository.findByVolunteerId(VolunteerID.from(cmd.volunteerId()))
                .orElseThrow(() -> new VolunteerNotFoundException("Volunteer not found with id: " + cmd.volunteerId()));

        if (!volunteer.canConsumeCredit())
            throw new InsufficientCreditsException("Volunteer has insufficient credits");

        DrinkTicket drinkTicket = DrinkTicket.pending(
                volunteer.getVolunteerId(),
                DrinkType.valueOf(cmd.drinkType().toUpperCase())
        );

        DrinkTicket savedDrinkTicket = drinkTicketRepository.save(drinkTicket);

        return CreateDrinkTicketResult.from(savedDrinkTicket);
    }
}
