package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccountStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListDrinkCardAccountsServiceTest {

    @Mock
    private DrinkCardAccountRepository drinkCardAccountRepository;

    @InjectMocks
    private ListDrinkCardAccountsService service;

    @Test
    void execute_WhenAccountsExist_ReturnsDrinkCardAccountSummaries() {
        VolunteerID firstVolunteerId = VolunteerID.generate();
        VolunteerID secondVolunteerId = VolunteerID.generate();

        Instant firstLastPurchase = Instant.parse("2026-05-18T10:00:00Z");

        DrinkCardAccount activeAccount = DrinkCardAccount.rehydrate(
                1L,
                firstVolunteerId,
                4,
                firstLastPurchase,
                Instant.parse("2026-05-17T10:00:00Z"),
                DrinkCardAccountStatus.ACTIVE
        );

        DrinkCardAccount suspendedAccount = DrinkCardAccount.rehydrate(
                2L,
                secondVolunteerId,
                0,
                null,
                Instant.parse("2026-05-17T11:00:00Z"),
                DrinkCardAccountStatus.SUSPENDED
        );

        when(drinkCardAccountRepository.findAll())
                .thenReturn(List.of(activeAccount, suspendedAccount));

        List<DrinkCardAccountSummaryResult> result = service.execute();

        assertAll(
                () -> assertEquals(2, result.size()),

                () -> assertEquals(firstVolunteerId.asString(), result.get(0).volunteerId()),
                () -> assertEquals(4, result.get(0).credits()),
                () -> assertEquals("ACTIVE", result.get(0).status()),
                () -> assertEquals(firstLastPurchase, result.get(0).lastPurchaseTimestamp()),

                () -> assertEquals(secondVolunteerId.asString(), result.get(1).volunteerId()),
                () -> assertEquals(0, result.get(1).credits()),
                () -> assertEquals("SUSPENDED", result.get(1).status()),
                () -> assertNull(result.get(1).lastPurchaseTimestamp())
        );

        verify(drinkCardAccountRepository).findAll();
    }

    @Test
    void execute_WhenNoAccountsExist_ReturnsEmptyList() {
        when(drinkCardAccountRepository.findAll())
                .thenReturn(List.of());

        List<DrinkCardAccountSummaryResult> result = service.execute();

        assertTrue(result.isEmpty());

        verify(drinkCardAccountRepository).findAll();
    }
}
