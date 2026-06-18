package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.EnableDrinkCardAccountRefillCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.InvalidDrinkCardAccountStatusTransitionException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkCardAccountStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnableDrinkCardAccountRefillServiceTest {

    @Mock
    private DrinkCardAccountRepository drinkCardAccountRepository;

    @InjectMocks
    private EnableDrinkCardAccountRefillService service;

    @Test
    void execute_WhenAccountHasRefillDisabled_EnablesRefillAndReturnsSummary() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount account = DrinkCardAccount.rehydrate(
                1L,
                volunteerId,
                2,
                null,
                Instant.now(),
                DrinkCardAccountStatus.REFILL_DISABLED
        );

        when(drinkCardAccountRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(account));
        when(drinkCardAccountRepository.save(any(DrinkCardAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DrinkCardAccountSummaryResult result = service.execute(
                new EnableDrinkCardAccountRefillCommand(volunteerId.asString())
        );

        ArgumentCaptor<DrinkCardAccount> captor = ArgumentCaptor.forClass(DrinkCardAccount.class);
        verify(drinkCardAccountRepository).save(captor.capture());

        assertAll(
                () -> assertEquals(DrinkCardAccountStatus.ACTIVE, captor.getValue().getStatus()),
                () -> assertEquals(volunteerId.asString(), result.volunteerId()),
                () -> assertEquals("ACTIVE", result.status())
        );
    }

    @Test
    void execute_WhenAccountIsAlreadyActive_IsIdempotent() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount account = DrinkCardAccount.rehydrate(
                1L,
                volunteerId,
                2,
                null,
                Instant.now(),
                DrinkCardAccountStatus.ACTIVE
        );

        when(drinkCardAccountRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(account));
        when(drinkCardAccountRepository.save(any(DrinkCardAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DrinkCardAccountSummaryResult result = service.execute(
                new EnableDrinkCardAccountRefillCommand(volunteerId.asString())
        );

        assertEquals("ACTIVE", result.status());
        verify(drinkCardAccountRepository).save(any(DrinkCardAccount.class));
    }

    @Test
    void execute_WhenAccountIsSuspended_ThrowsInvalidStatusTransitionAndDoesNotSave() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount account = DrinkCardAccount.rehydrate(
                1L,
                volunteerId,
                0,
                null,
                Instant.now(),
                DrinkCardAccountStatus.SUSPENDED
        );

        when(drinkCardAccountRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(account));

        assertThrows(
                InvalidDrinkCardAccountStatusTransitionException.class,
                () -> service.execute(new EnableDrinkCardAccountRefillCommand(volunteerId.asString()))
        );

        verify(drinkCardAccountRepository, never()).save(any(DrinkCardAccount.class));
    }

    @Test
    void execute_WhenAccountDoesNotExist_ThrowsNotFound() {
        VolunteerID volunteerId = VolunteerID.generate();

        when(drinkCardAccountRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.empty());

        assertThrows(
                DrinkCardAccountNotFoundException.class,
                () -> service.execute(new EnableDrinkCardAccountRefillCommand(volunteerId.asString()))
        );

        verify(drinkCardAccountRepository, never()).save(any(DrinkCardAccount.class));
    }
}
