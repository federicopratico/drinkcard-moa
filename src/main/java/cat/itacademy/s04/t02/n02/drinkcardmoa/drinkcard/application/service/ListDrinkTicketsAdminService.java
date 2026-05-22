package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListDrinkTicketsAdminQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkTicketSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListDrinkTicketsAdminUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.DrinkTicketSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicketStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSort;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSortParser;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class ListDrinkTicketsAdminService implements ListDrinkTicketsAdminUseCase {

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
            "status",
            "volunteerId"
    );

    private final DrinkTicketRepository drinkTicketRepository;

    public ListDrinkTicketsAdminService(DrinkTicketRepository drinkTicketRepository) {
        this.drinkTicketRepository = drinkTicketRepository;
    }

    @Override
    public PageResult<DrinkTicketSummaryResult> execute(ListDrinkTicketsAdminQuery query) {
        DrinkTicketSearchCriteria criteria = toSearchCriteria(query);

        return drinkTicketRepository.searchAdminDrinkTickets(criteria)
                .map(DrinkTicketSummaryResult::from);
    }

    private DrinkTicketSearchCriteria toSearchCriteria(ListDrinkTicketsAdminQuery query) {
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
                "drink ticket"
        );

        return new DrinkTicketSearchCriteria(
                parseVolunteerId(query.volunteerId()),
                parseStatus(query.status()),
                query.from(),
                query.to(),
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

    private DrinkTicketStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        return DrinkTicketStatus.valueOf(status.toUpperCase(Locale.ROOT));
    }
}
