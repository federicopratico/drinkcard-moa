package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.CreateDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.CreateDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.VolunteerRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.exception.InsufficientCreditsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.exception.VolunteerNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.DrinkTicketStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.DrinkType;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Volunteer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDrinkTicketServiceTest {

    @Mock
    private DrinkTicketRepository drinkTicketRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @InjectMocks
    private CreateDrinkTicketService service;

    @Test
    void execute_WhenVolunteerHasCredits_ShouldCreateAndSavePendingDrinkTicket() {
        VolunteerID volunteerId = VolunteerID.generate();
        Volunteer volunteer = createVolunteerWithCredits(volunteerId);

        CreateDrinkTicketCommand command = new CreateDrinkTicketCommand(
                volunteerId.asString(),
                "BEER"
        );

        when(volunteerRepository.findByVolunteerId(VolunteerID.from(command.volunteerId())))
                .thenReturn(Optional.of(volunteer));

        when(drinkTicketRepository.save(any(DrinkTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateDrinkTicketResult result = service.execute(command);

        ArgumentCaptor<DrinkTicket> drinkTicketCaptor = ArgumentCaptor.forClass(DrinkTicket.class);

        verify(volunteerRepository).findByVolunteerId(VolunteerID.from(command.volunteerId()));
        verify(drinkTicketRepository).save(drinkTicketCaptor.capture());

        DrinkTicket savedDrinkTicket = drinkTicketCaptor.getValue();

        assertAll(
                () -> assertNotNull(savedDrinkTicket.getDrinkTicketId()),
                () -> assertEquals(volunteerId, savedDrinkTicket.getVolunteerId()),
                () -> assertEquals(DrinkType.BEER, savedDrinkTicket.getDrinkType()),
                () -> assertEquals(DrinkTicketStatus.PENDING, savedDrinkTicket.getStatus()),
                () -> assertNotNull(savedDrinkTicket.getCreatedAt()),
                () -> assertNotNull(savedDrinkTicket.getExpiresAt()),
                () -> assertEquals(savedDrinkTicket.getDrinkTicketId().asString(), result.ticketId()),
                () -> assertEquals("BEER", result.drinkType()),
                () -> assertEquals("PENDING", result.status()),
                () -> assertEquals(savedDrinkTicket.getExpiresAt(), result.expiresAt())
        );
    }

    @Test
    void execute_WhenDrinkTypeIsLowercase_ShouldCreateDrinkTicket() {
        VolunteerID volunteerId = VolunteerID.generate();
        Volunteer volunteer = createVolunteerWithCredits(volunteerId);

        CreateDrinkTicketCommand command = new CreateDrinkTicketCommand(
                volunteerId.asString(),
                "water"
        );

        when(volunteerRepository.findByVolunteerId(VolunteerID.from(command.volunteerId())))
                .thenReturn(Optional.of(volunteer));

        when(drinkTicketRepository.save(any(DrinkTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateDrinkTicketResult result = service.execute(command);

        ArgumentCaptor<DrinkTicket> drinkTicketCaptor = ArgumentCaptor.forClass(DrinkTicket.class);

        verify(drinkTicketRepository).save(drinkTicketCaptor.capture());

        DrinkTicket savedDrinkTicket = drinkTicketCaptor.getValue();

        assertAll(
                () -> assertEquals(DrinkType.WATER, savedDrinkTicket.getDrinkType()),
                () -> assertEquals("WATER", result.drinkType())
        );
    }

    @Test
    void execute_WhenVolunteerDoesNotExist_ShouldThrowVolunteerNotFoundException() {
        VolunteerID volunteerId = VolunteerID.generate();

        CreateDrinkTicketCommand command = new CreateDrinkTicketCommand(
                volunteerId.asString(),
                "BEER"
        );

        when(volunteerRepository.findByVolunteerId(VolunteerID.from(command.volunteerId())))
                .thenReturn(Optional.empty());

        assertThrows(
                VolunteerNotFoundException.class,
                () -> service.execute(command)
        );

        verify(volunteerRepository).findByVolunteerId(VolunteerID.from(command.volunteerId()));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    @Test
    void execute_WhenVolunteerHasNoCredits_ShouldThrowInsufficientCreditsException() {
        VolunteerID volunteerId = VolunteerID.generate();
        Volunteer volunteer = Volunteer.create(volunteerId);

        CreateDrinkTicketCommand command = new CreateDrinkTicketCommand(
                volunteerId.asString(),
                "BEER"
        );

        when(volunteerRepository.findByVolunteerId(VolunteerID.from(command.volunteerId())))
                .thenReturn(Optional.of(volunteer));

        assertThrows(
                InsufficientCreditsException.class,
                () -> service.execute(command)
        );

        verify(volunteerRepository).findByVolunteerId(VolunteerID.from(command.volunteerId()));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    @Test
    void execute_WhenDrinkTypeIsInvalid_ShouldThrowIllegalArgumentException() {
        VolunteerID volunteerId = VolunteerID.generate();
        Volunteer volunteer = createVolunteerWithCredits(volunteerId);

        CreateDrinkTicketCommand command = new CreateDrinkTicketCommand(
                volunteerId.asString(),
                "INVALID_DRINK"
        );

        when(volunteerRepository.findByVolunteerId(VolunteerID.from(command.volunteerId())))
                .thenReturn(Optional.of(volunteer));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(command)
        );

        verify(volunteerRepository).findByVolunteerId(VolunteerID.from(command.volunteerId()));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    private Volunteer createVolunteerWithCredits(VolunteerID volunteerId) {
        Volunteer volunteer = Volunteer.create(volunteerId);
        volunteer.purchaseCard(Card.newCard(), Instant.now());
        volunteer.getDomainEvents();

        return volunteer;
    }
}
