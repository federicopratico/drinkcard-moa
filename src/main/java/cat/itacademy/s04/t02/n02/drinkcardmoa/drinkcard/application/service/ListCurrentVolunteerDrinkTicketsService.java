package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListCurrentVolunteerDrinkTicketsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkTicketSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListCurrentVolunteerDrinkTicketsUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.DrinkTicketSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSort;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSortParser;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ListCurrentVolunteerDrinkTicketsService implements ListCurrentVolunteerDrinkTicketsUseCase {

    private static final String DEFAULT_SORT_BY = "createdAt";
    private static final String DEFAULT_SORT_DIRECTION = "desc";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "expiresAt",
            "drinkType",
            "consumedAt",
            "status"
    );

    private final DrinkTicketRepository drinkTicketRepository;

    public ListCurrentVolunteerDrinkTicketsService(DrinkTicketRepository drinkTicketRepository) {
        this.drinkTicketRepository = drinkTicketRepository;
    }

    @Override
    public PageResult<DrinkTicketSummaryResult> execute(ListCurrentVolunteerDrinkTicketsQuery query) {
        DrinkTicketSearchCriteria criteria = toSearchCriteria(query);

        return drinkTicketRepository.searchVolunteerDrinkTickets(criteria)
                .map(DrinkTicketSummaryResult::from);
    }

    private DrinkTicketSearchCriteria toSearchCriteria(ListCurrentVolunteerDrinkTicketsQuery query) {
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
                "drink-ticket"
        );

        return new DrinkTicketSearchCriteria(
                VolunteerID.from(query.volunteerId()),
                null,
                null,
                null,
                pageSort.page(),
                pageSort.size(),
                pageSort.sortBy(),
                pageSort.sortDirection()
        );
    }
}
