package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.ConsumeDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConsumeDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.VolunteerRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.*;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicketID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicketStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkType;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.Volunteer;
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
class ConfirmDrinkTicketServiceTest {

    @Mock
    private DrinkTicketRepository drinkTicketRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @InjectMocks
    private ConsumeDrinkTicketService service;

    @Test
    void execute_WhenTicketIsPendingAndVolunteerHasCredits_ShouldConsumeTicketAndCredit() {
        VolunteerID volunteerId = VolunteerID.generate();
        Volunteer volunteer = createVolunteerWithCredits(volunteerId);
        DrinkTicket drinkTicket = DrinkTicket.pending(volunteerId, DrinkType.BEER);

        ConsumeDrinkTicketCommand command = new ConsumeDrinkTicketCommand(
                drinkTicket.getDrinkTicketId().asString(),
                "staff-123"
        );

        when(drinkTicketRepository.findByDrinkTicketId(DrinkTicketID.from(command.ticketId())))
                .thenReturn(Optional.of(drinkTicket));

        when(volunteerRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(volunteer));

        when(volunteerRepository.save(any(Volunteer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(drinkTicketRepository.save(any(DrinkTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConsumeDrinkTicketResult result = service.execute(command);

        ArgumentCaptor<Volunteer> volunteerCaptor = ArgumentCaptor.forClass(Volunteer.class);
        ArgumentCaptor<DrinkTicket> drinkTicketCaptor = ArgumentCaptor.forClass(DrinkTicket.class);

        verify(volunteerRepository).save(volunteerCaptor.capture());
        verify(drinkTicketRepository).save(drinkTicketCaptor.capture());

        Volunteer savedVolunteer = volunteerCaptor.getValue();
        DrinkTicket savedDrinkTicket = drinkTicketCaptor.getValue();

        assertAll(
                () -> assertEquals(4, savedVolunteer.getCredits()),
                () -> assertEquals(DrinkTicketStatus.CONSUMED, savedDrinkTicket.getStatus()),
                () -> assertNotNull(savedDrinkTicket.getConsumedAt()),
                () -> assertEquals("staff-123", savedDrinkTicket.getConsumedByStaffId()),
                () -> assertEquals(savedDrinkTicket.getDrinkTicketId().asString(), result.ticketId()),
                () -> assertEquals("CONSUMED", result.status()),
                () -> assertEquals("BEER", result.drinkType()),
                () -> assertEquals(4, result.remainingCredits())
        );
    }

    @Test
    void execute_WhenTicketDoesNotExist_ShouldThrowDrinkTicketNotFoundException() {
        DrinkTicketID drinkTicketId = DrinkTicketID.generate();

        ConsumeDrinkTicketCommand command = new ConsumeDrinkTicketCommand(
                drinkTicketId.asString(),
                "staff-123"
        );

        when(drinkTicketRepository.findByDrinkTicketId(DrinkTicketID.from(command.ticketId())))
                .thenReturn(Optional.empty());

        assertThrows(
                DrinkTicketNotFoundException.class,
                () -> service.execute(command)
        );

        verify(volunteerRepository, never()).findByVolunteerId(any(VolunteerID.class));
        verify(volunteerRepository, never()).save(any(Volunteer.class));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    @Test
    void execute_WhenVolunteerDoesNotExist_ShouldThrowVolunteerNotFoundException() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkTicket drinkTicket = DrinkTicket.pending(volunteerId, DrinkType.BEER);

        ConsumeDrinkTicketCommand command = new ConsumeDrinkTicketCommand(
                drinkTicket.getDrinkTicketId().asString(),
                "staff-123"
        );

        when(drinkTicketRepository.findByDrinkTicketId(DrinkTicketID.from(command.ticketId())))
                .thenReturn(Optional.of(drinkTicket));

        when(volunteerRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.empty());

        assertThrows(
                VolunteerNotFoundException.class,
                () -> service.execute(command)
        );

        verify(volunteerRepository).findByVolunteerId(volunteerId);
        verify(volunteerRepository, never()).save(any(Volunteer.class));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    @Test
    void execute_WhenTicketIsExpired_ShouldThrowDrinkTicketExpiredException() {
        VolunteerID volunteerId = VolunteerID.generate();
        Volunteer volunteer = createVolunteerWithCredits(volunteerId);

        Instant createdAt = Instant.now().minusSeconds(180);
        Instant expiresAt = createdAt.plusSeconds(90);

        DrinkTicket drinkTicket = DrinkTicket.rehydrate(
                DrinkTicketID.generate(),
                volunteerId,
                DrinkType.BEER,
                DrinkTicketStatus.PENDING,
                createdAt,
                expiresAt,
                null,
                null
        );

        ConsumeDrinkTicketCommand command = new ConsumeDrinkTicketCommand(
                drinkTicket.getDrinkTicketId().asString(),
                "staff-123"
        );

        when(drinkTicketRepository.findByDrinkTicketId(DrinkTicketID.from(command.ticketId())))
                .thenReturn(Optional.of(drinkTicket));

        when(volunteerRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(volunteer));

        assertThrows(
                DrinkTicketExpiredException.class,
                () -> service.execute(command)
        );

        verify(volunteerRepository, never()).save(any(Volunteer.class));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    @Test
    void execute_WhenTicketIsAlreadyConsumed_ShouldThrowInvalidDrinkTicketStateException() {
        VolunteerID volunteerId = VolunteerID.generate();
        Volunteer volunteer = createVolunteerWithCredits(volunteerId);

        DrinkTicket drinkTicket = DrinkTicket.rehydrate(
                DrinkTicketID.generate(),
                volunteerId,
                DrinkType.BEER,
                DrinkTicketStatus.CONSUMED,
                Instant.now().minusSeconds(30),
                Instant.now().plusSeconds(60),
                Instant.now().minusSeconds(10),
                "staff-123"
        );

        ConsumeDrinkTicketCommand command = new ConsumeDrinkTicketCommand(
                drinkTicket.getDrinkTicketId().asString(),
                "staff-456"
        );

        when(drinkTicketRepository.findByDrinkTicketId(DrinkTicketID.from(command.ticketId())))
                .thenReturn(Optional.of(drinkTicket));

        when(volunteerRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(volunteer));

        assertThrows(
                InvalidDrinkTicketStateException.class,
                () -> service.execute(command)
        );

        verify(volunteerRepository, never()).save(any(Volunteer.class));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    @Test
    void execute_WhenVolunteerHasNoCredits_ShouldThrowInsufficientCreditsException() {
        VolunteerID volunteerId = VolunteerID.generate();
        Volunteer volunteer = Volunteer.create(volunteerId);
        DrinkTicket drinkTicket = DrinkTicket.pending(volunteerId, DrinkType.BEER);

        ConsumeDrinkTicketCommand command = new ConsumeDrinkTicketCommand(
                drinkTicket.getDrinkTicketId().asString(),
                "staff-123"
        );

        when(drinkTicketRepository.findByDrinkTicketId(DrinkTicketID.from(command.ticketId())))
                .thenReturn(Optional.of(drinkTicket));

        when(volunteerRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(volunteer));

        assertThrows(
                InsufficientCreditsException.class,
                () -> service.execute(command)
        );

        verify(volunteerRepository, never()).save(any(Volunteer.class));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    private Volunteer createVolunteerWithCredits(VolunteerID volunteerId) {
        Volunteer volunteer = Volunteer.create(volunteerId);
        volunteer.purchaseCard(Card.newCard(), Instant.now());
        volunteer.getDomainEvents();

        return volunteer;
    }
}
