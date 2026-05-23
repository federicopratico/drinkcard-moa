package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreateDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.ActiveDrinkTicketAlreadyExistsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountSuspendedException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.InsufficientCreditsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkCardAccountStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkTicketStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkType;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDrinkTicketServiceTest {

    @Mock
    private DrinkTicketRepository drinkTicketRepository;

    @Mock
    private DrinkCardAccountRepository drinkCardAccountRepository;

    @InjectMocks
    private CreateDrinkTicketService service;

    @Test
    void execute_WhenDrinkCardAccountHasCredits_ShouldCreateAndSavePendingDrinkTicket() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = createDrinkCardAccountWithCredits(volunteerId);

        CreateDrinkTicketCommand command = new CreateDrinkTicketCommand(
                volunteerId.asString(),
                "BEER"
        );

        when(drinkCardAccountRepository.findByVolunteerId(VolunteerID.from(command.volunteerId())))
                .thenReturn(Optional.of(drinkCardAccount));

        when(drinkTicketRepository.existsActivePendingByVolunteerId(
                eq(volunteerId),
                any(Instant.class)
        )).thenReturn(false);

        when(drinkTicketRepository.save(any(DrinkTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateDrinkTicketResult result = service.execute(command);

        ArgumentCaptor<DrinkTicket> drinkTicketCaptor = ArgumentCaptor.forClass(DrinkTicket.class);

        verify(drinkCardAccountRepository).findByVolunteerId(VolunteerID.from(command.volunteerId()));
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
        DrinkCardAccount drinkCardAccount = createDrinkCardAccountWithCredits(volunteerId);

        CreateDrinkTicketCommand command = new CreateDrinkTicketCommand(
                volunteerId.asString(),
                "water"
        );

        when(drinkCardAccountRepository.findByVolunteerId(VolunteerID.from(command.volunteerId())))
                .thenReturn(Optional.of(drinkCardAccount));

        when(drinkTicketRepository.existsActivePendingByVolunteerId(
                eq(volunteerId),
                any(Instant.class)
        )).thenReturn(false);

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
    void execute_WhenDrinkCardAccountDoesNotExist_ShouldThrowDrinkCardAccountNotFoundException() {
        VolunteerID volunteerId = VolunteerID.generate();

        CreateDrinkTicketCommand command = new CreateDrinkTicketCommand(
                volunteerId.asString(),
                "BEER"
        );

        when(drinkCardAccountRepository.findByVolunteerId(VolunteerID.from(command.volunteerId())))
                .thenReturn(Optional.empty());

        assertThrows(
                DrinkCardAccountNotFoundException.class,
                () -> service.execute(command)
        );

        verify(drinkCardAccountRepository).findByVolunteerId(VolunteerID.from(command.volunteerId()));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    @Test
    void execute_WhenDrinkCardAccountHasNoCredits_ShouldThrowInsufficientCreditsException() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(volunteerId);

        CreateDrinkTicketCommand command = new CreateDrinkTicketCommand(
                volunteerId.asString(),
                "BEER"
        );

        when(drinkCardAccountRepository.findByVolunteerId(VolunteerID.from(command.volunteerId())))
                .thenReturn(Optional.of(drinkCardAccount));

        assertThrows(
                InsufficientCreditsException.class,
                () -> service.execute(command)
        );

        verify(drinkCardAccountRepository).findByVolunteerId(VolunteerID.from(command.volunteerId()));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    @Test
    void execute_WhenDrinkCardAccountIsSuspended_ShouldThrowDrinkCardAccountSuspendedException() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                volunteerId,
                5,
                null,
                Instant.now(),
                DrinkCardAccountStatus.SUSPENDED
        );

        CreateDrinkTicketCommand command = new CreateDrinkTicketCommand(
                volunteerId.asString(),
                "BEER"
        );

        when(drinkCardAccountRepository.findByVolunteerId(VolunteerID.from(command.volunteerId())))
                .thenReturn(Optional.of(drinkCardAccount));

        assertThrows(
                DrinkCardAccountSuspendedException.class,
                () -> service.execute(command)
        );

        verify(drinkCardAccountRepository).findByVolunteerId(VolunteerID.from(command.volunteerId()));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    @Test
    void execute_WhenVolunteerAlreadyHasActivePendingTicket_ShouldThrowActiveDrinkTicketAlreadyExistsException() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = createDrinkCardAccountWithCredits(volunteerId);

        CreateDrinkTicketCommand command = new CreateDrinkTicketCommand(
                volunteerId.asString(),
                "BEER"
        );

        when(drinkCardAccountRepository.findByVolunteerId(VolunteerID.from(command.volunteerId())))
                .thenReturn(Optional.of(drinkCardAccount));

        when(drinkTicketRepository.existsActivePendingByVolunteerId(
                eq(volunteerId),
                any(Instant.class)
        )).thenReturn(true);

        assertThrows(
                ActiveDrinkTicketAlreadyExistsException.class,
                () -> service.execute(command)
        );

        verify(drinkCardAccountRepository).findByVolunteerId(VolunteerID.from(command.volunteerId()));
        verify(drinkTicketRepository).existsActivePendingByVolunteerId(eq(volunteerId), any(Instant.class));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    @Test
    void execute_WhenDrinkTypeIsInvalid_ShouldThrowIllegalArgumentException() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = createDrinkCardAccountWithCredits(volunteerId);

        CreateDrinkTicketCommand command = new CreateDrinkTicketCommand(
                volunteerId.asString(),
                "INVALID_DRINK"
        );

        when(drinkCardAccountRepository.findByVolunteerId(VolunteerID.from(command.volunteerId())))
                .thenReturn(Optional.of(drinkCardAccount));

        when(drinkTicketRepository.existsActivePendingByVolunteerId(
                eq(volunteerId),
                any(Instant.class)
        )).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(command)
        );

        verify(drinkCardAccountRepository).findByVolunteerId(VolunteerID.from(command.volunteerId()));
        verify(drinkTicketRepository, never()).save(any(DrinkTicket.class));
    }

    private DrinkCardAccount createDrinkCardAccountWithCredits(VolunteerID volunteerId) {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(volunteerId);
        drinkCardAccount.purchaseCard(Card.newCard(), Instant.now());
        drinkCardAccount.getDomainEvents();

        return drinkCardAccount;
    }
}
