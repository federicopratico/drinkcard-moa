package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreateDrinkCardAccountCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateDrinkCardAccountResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateDrinkCardAccountServiceTest {

    @Mock
    private DrinkCardAccountRepository drinkCardAccountRepository;

    @InjectMocks
    private CreateDrinkCardAccountService createDrinkCardAccountService;

    @Test
    void execute_WhenDrinkCardAccountDoesNotExist_ShouldCreateAndSaveNewAccount() {
        VolunteerID volunteerID = VolunteerID.generate();
        CreateDrinkCardAccountCommand command = new CreateDrinkCardAccountCommand(volunteerID.asString());

        when(drinkCardAccountRepository.existsByVolunteerId(volunteerID)).thenReturn(false);
        when(drinkCardAccountRepository.save(any(DrinkCardAccount.class))).thenAnswer(i -> i.getArgument(0));

        CreateDrinkCardAccountResult result = createDrinkCardAccountService.execute(command);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(volunteerID.asString(), result.volunteerID()),
                () -> assertEquals(0, result.credits())
        );

        verify(drinkCardAccountRepository, times(1)).existsByVolunteerId(volunteerID);
        verify(drinkCardAccountRepository, times(1)).save(any(DrinkCardAccount.class));
    }

    @Test
    void execute_WhenDrinkCardAccountAlreadyExists_ShouldReturnExistingAccount() {
        VolunteerID volunteerID = VolunteerID.generate();
        CreateDrinkCardAccountCommand command = new CreateDrinkCardAccountCommand(volunteerID.asString());

        DrinkCardAccount existingDrinkCardAccount = DrinkCardAccount.rehydrate(1L, volunteerID, 5, Instant.now(), Instant.now());

        when(drinkCardAccountRepository.existsByVolunteerId(volunteerID)).thenReturn(true);
        when(drinkCardAccountRepository.findByVolunteerId(volunteerID)).thenReturn(java.util.Optional.of(existingDrinkCardAccount));

        CreateDrinkCardAccountResult result = createDrinkCardAccountService.execute(command);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(volunteerID.asString(), result.volunteerID()),
                () -> assertEquals(5, result.credits())
        );

        verify(drinkCardAccountRepository, times(1)).existsByVolunteerId(volunteerID);
        verify(drinkCardAccountRepository, times(1)).findByVolunteerId(volunteerID);
        verify(drinkCardAccountRepository, never()).save(any(DrinkCardAccount.class));
    }

    @Test
    void execute_WhenDrinkCardAccountAlreadyExistsButCannotBeFound_ShouldThrowException() {
        VolunteerID volunteerId = VolunteerID.generate();
        CreateDrinkCardAccountCommand command = new CreateDrinkCardAccountCommand(volunteerId.asString());

        when(drinkCardAccountRepository.existsByVolunteerId(volunteerId)).thenReturn(true);
        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> createDrinkCardAccountService.execute(command));

        verify(drinkCardAccountRepository).existsByVolunteerId(volunteerId);
        verify(drinkCardAccountRepository).findByVolunteerId(volunteerId);
        verify(drinkCardAccountRepository, never()).save(any(DrinkCardAccount.class));
    }

    @Test
    void execute_WhenVolunteerIdIsInvalid_ShouldThrowException() {
        CreateDrinkCardAccountCommand command = new CreateDrinkCardAccountCommand("invalid-uuid");

        assertThrows(IllegalArgumentException.class, () -> createDrinkCardAccountService.execute(command));

        verifyNoInteractions(drinkCardAccountRepository);
    }
 }
