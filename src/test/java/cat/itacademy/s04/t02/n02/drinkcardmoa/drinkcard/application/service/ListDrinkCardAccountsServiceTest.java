package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListDrinkCardAccountsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.DrinkCardAccountSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccountStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListDrinkCardAccountsServiceTest {

    @Mock
    private DrinkCardAccountRepository drinkCardAccountRepository;

    @InjectMocks
    private ListDrinkCardAccountsService service;

    @Test
    void execute_WhenNoFiltersProvided_ShouldReturnPagedAccountsWithDefaults() {
        Instant lastPurchase = Instant.parse("2026-05-18T10:00:00Z");
        DrinkCardAccount account = createAccount(DrinkCardAccountStatus.ACTIVE, 4, lastPurchase);

        when(drinkCardAccountRepository.searchDrinkCardAccounts(any(DrinkCardAccountSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(account), 0, 20, 1, 1));

        PageResult<DrinkCardAccountSummaryResult> result = service.execute(
                new ListDrinkCardAccountsQuery(null, null, -1, 0, null)
        );

        ArgumentCaptor<DrinkCardAccountSearchCriteria> criteriaCaptor =
                ArgumentCaptor.forClass(DrinkCardAccountSearchCriteria.class);
        verify(drinkCardAccountRepository).searchDrinkCardAccounts(criteriaCaptor.capture());

        DrinkCardAccountSearchCriteria criteria = criteriaCaptor.getValue();
        DrinkCardAccountSummaryResult accountResult = result.content().getFirst();

        assertAll(
                () -> assertNull(criteria.volunteerId()),
                () -> assertNull(criteria.status()),
                () -> assertEquals(0, criteria.page()),
                () -> assertEquals(20, criteria.size()),
                () -> assertEquals("volunteerId", criteria.sortBy()),
                () -> assertEquals("asc", criteria.sortDirection()),
                () -> assertEquals(1, result.content().size()),
                () -> assertEquals(account.getVolunteerId().asString(), accountResult.volunteerId()),
                () -> assertEquals(4, accountResult.credits()),
                () -> assertEquals("ACTIVE", accountResult.status()),
                () -> assertEquals(lastPurchase, accountResult.lastPurchaseTimestamp())
        );
    }

    @Test
    void execute_WhenFiltersProvided_ShouldPassParsedCriteriaToRepository() {
        VolunteerID volunteerId = VolunteerID.generate();

        when(drinkCardAccountRepository.searchDrinkCardAccounts(any(DrinkCardAccountSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(), 2, 10, 0, 0));

        service.execute(new ListDrinkCardAccountsQuery(
                volunteerId.asString(),
                "suspended",
                2,
                10,
                "credits,desc"
        ));

        ArgumentCaptor<DrinkCardAccountSearchCriteria> criteriaCaptor =
                ArgumentCaptor.forClass(DrinkCardAccountSearchCriteria.class);
        verify(drinkCardAccountRepository).searchDrinkCardAccounts(criteriaCaptor.capture());

        DrinkCardAccountSearchCriteria criteria = criteriaCaptor.getValue();

        assertAll(
                () -> assertEquals(volunteerId, criteria.volunteerId()),
                () -> assertEquals(DrinkCardAccountStatus.SUSPENDED, criteria.status()),
                () -> assertEquals(2, criteria.page()),
                () -> assertEquals(10, criteria.size()),
                () -> assertEquals("credits", criteria.sortBy()),
                () -> assertEquals("desc", criteria.sortDirection())
        );
    }

    @Test
    void execute_WhenSizeExceedsMaximum_ShouldCapPageSize() {
        when(drinkCardAccountRepository.searchDrinkCardAccounts(any(DrinkCardAccountSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(), 0, 100, 0, 0));

        service.execute(new ListDrinkCardAccountsQuery(
                null,
                null,
                0,
                150,
                "createdAt,desc"
        ));

        ArgumentCaptor<DrinkCardAccountSearchCriteria> criteriaCaptor =
                ArgumentCaptor.forClass(DrinkCardAccountSearchCriteria.class);
        verify(drinkCardAccountRepository).searchDrinkCardAccounts(criteriaCaptor.capture());

        DrinkCardAccountSearchCriteria criteria = criteriaCaptor.getValue();

        assertAll(
                () -> assertEquals(100, criteria.size()),
                () -> assertEquals("createdAt", criteria.sortBy()),
                () -> assertEquals("desc", criteria.sortDirection())
        );
    }

    @Test
    void execute_WhenStatusIsInvalid_ShouldThrowIllegalArgumentException() {
        ListDrinkCardAccountsQuery query = new ListDrinkCardAccountsQuery(
                null,
                "UNKNOWN",
                0,
                20,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(query)
        );
    }

    private DrinkCardAccount createAccount(
            DrinkCardAccountStatus status,
            int credits,
            Instant lastPurchaseTimestamp
    ) {
        return DrinkCardAccount.rehydrate(
                1L,
                VolunteerID.generate(),
                credits,
                lastPurchaseTimestamp,
                Instant.parse("2026-05-17T10:00:00Z"),
                status
        );
    }
}
