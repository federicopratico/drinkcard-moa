package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.ConsumeDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.ConsumeDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase.ConsumeDrinkTicketUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.VolunteerRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.exception.DrinkTicketNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.exception.VolunteerNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.DrinkTicketID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Volunteer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ConsumeDrinkTicketService implements ConsumeDrinkTicketUseCase {

    private final DrinkTicketRepository drinkTicketRepository;
    private final VolunteerRepository volunteerRepository;

    public ConsumeDrinkTicketService(DrinkTicketRepository drinkTicketRepository, VolunteerRepository volunteerRepository) {
        this.drinkTicketRepository = drinkTicketRepository;
        this.volunteerRepository = volunteerRepository;
    }

    @Transactional
    @Override
    public ConsumeDrinkTicketResult execute(ConsumeDrinkTicketCommand cmd) {
        DrinkTicket drinkTicket = drinkTicketRepository.findByDrinkTicketId(DrinkTicketID.from(cmd.ticketId()))
                .orElseThrow(() -> new DrinkTicketNotFoundException("Drink ticket not found."));

        Volunteer volunteer = volunteerRepository.findByVolunteerId(drinkTicket.getVolunteerId())
                .orElseThrow(() -> new VolunteerNotFoundException(
                        "Volunteer not found with id: " + drinkTicket.getVolunteerId().asString()));

        volunteer.consumeCredit();
        drinkTicket.consume(cmd.consumedByStaffId(), Instant.now());

        Volunteer savedVolunteer = volunteerRepository.save(volunteer);
        DrinkTicket savedDrinkTicket = drinkTicketRepository.save(drinkTicket);

        return toConsumeDrinkTicketResult(savedDrinkTicket, savedVolunteer);
    }

    private ConsumeDrinkTicketResult toConsumeDrinkTicketResult(DrinkTicket drinkTicket, Volunteer volunteer) {
        return new ConsumeDrinkTicketResult(
                drinkTicket.getDrinkTicketId().asString(),
                drinkTicket.getStatus().toString(),
                drinkTicket.getDrinkType().toString(),
                volunteer.getCredits()
        );
    }
}
