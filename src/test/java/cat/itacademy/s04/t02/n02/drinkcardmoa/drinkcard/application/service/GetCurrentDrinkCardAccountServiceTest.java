package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetCurrentDrinkCardAccountQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CurrentDrinkCardAccountResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccountStatus;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentDrinkCardAccountServiceTest {

    @Mock
    private DrinkCardAccountRepository drinkCardAccountRepository;

    @InjectMocks
    private GetCurrentDrinkCardAccountService service;

    @Test
    void execute_WhenDrinkCardAccountExists_ReturnCurrentDrinkCardAccount() {
        VolunteerID volunteerId = VolunteerID.generate();
        Instant lastPurchaseTimestamp = Instant.parse("2026-05-18T10:00:00Z");
        DrinkCardAccount account = DrinkCardAccount.rehydrate(
                1L,
                volunteerId,
                4,
                lastPurchaseTimestamp,
                Instant.parse("2026-05-17T10:00:00Z"),
                DrinkCardAccountStatus.ACTIVE
        );

        when(drinkCardAccountRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.of(account));

        CurrentDrinkCardAccountResult result = service.execute(
                new GetCurrentDrinkCardAccountQuery(volunteerId.asString())
        );

        assertAll(
                () -> assertEquals(volunteerId.asString(), result.volunteerId()),
                () -> assertEquals(4, result.credits()),
                () -> assertEquals("ACTIVE", result.status()),
                () -> assertEquals(lastPurchaseTimestamp, result.lastPurchaseTimestamp())
        );

        ArgumentCaptor<VolunteerID> volunteerIdCaptor = ArgumentCaptor.forClass(VolunteerID.class);
        verify(drinkCardAccountRepository).findByVolunteerId(volunteerIdCaptor.capture());
        assertEquals(volunteerId.asString(), volunteerIdCaptor.getValue().asString());
    }

    @Test
    void execute_WhenDrinkCardAccountDoesNotExist_ThrowDrinkCardAccountNotFoundException() {
        VolunteerID volunteerId = VolunteerID.generate();

        when(drinkCardAccountRepository.findByVolunteerId(volunteerId))
                .thenReturn(Optional.empty());

        assertThrows(
                DrinkCardAccountNotFoundException.class,
                () -> service.execute(new GetCurrentDrinkCardAccountQuery(volunteerId.asString()))
        );

        verify(drinkCardAccountRepository).findByVolunteerId(volunteerId);
    }
}
