package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListDrinkCardAccountsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListDrinkCardAccountsUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.DrinkCardAccountSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkCardAccountStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSort;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSortParser;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class ListDrinkCardAccountsService implements ListDrinkCardAccountsUseCase {

    private static final String DEFAULT_SORT_BY = "volunteerId";
    private static final String DEFAULT_SORT_DIRECTION = "asc";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "volunteerId",
            "credits",
            "status",
            "lastPurchaseTimestamp",
            "createdAt"
    );

    private final DrinkCardAccountRepository drinkCardAccountRepository;

    public ListDrinkCardAccountsService(DrinkCardAccountRepository drinkCardAccountRepository) {
        this.drinkCardAccountRepository = drinkCardAccountRepository;
    }

    @Override
    public PageResult<DrinkCardAccountSummaryResult> execute(ListDrinkCardAccountsQuery query) {
        DrinkCardAccountSearchCriteria criteria = toSearchCriteria(query);

        return drinkCardAccountRepository.searchDrinkCardAccounts(criteria)
                .map(DrinkCardAccountSummaryResult::from);
    }

    private DrinkCardAccountSearchCriteria toSearchCriteria(ListDrinkCardAccountsQuery query) {
        PageSort pageSort = PageSortParser.parse(
                query.page(),
                query.size(),
                query.sort(),
                DEFAULT_SORT_BY,
                DEFAULT_SORT_DIRECTION,
                DEFAULT_PAGE,
                DEFAULT_SIZE,
                MAX_SIZE,
                ALLOWED_SORT_FIELDS,
                "drink card account"
        );

        return new DrinkCardAccountSearchCriteria(
                parseVolunteerId(query.volunteerId()),
                parseStatus(query.status()),
                pageSort.page(),
                pageSort.size(),
                pageSort.sortBy(),
                pageSort.sortDirection()
        );
    }

    private VolunteerID parseVolunteerId(String volunteerId) {
        if (volunteerId == null || volunteerId.isBlank()) {
            return null;
        }

        return VolunteerID.from(volunteerId);
    }

    private DrinkCardAccountStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        return DrinkCardAccountStatus.valueOf(status.toUpperCase(Locale.ROOT));
    }
}
