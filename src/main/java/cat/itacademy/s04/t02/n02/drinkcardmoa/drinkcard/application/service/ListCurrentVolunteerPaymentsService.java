package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListCurrentVolunteerPaymentsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListCurrentVolunteerPaymentsUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.PaymentSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSort;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSortParser;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ListCurrentVolunteerPaymentsService implements ListCurrentVolunteerPaymentsUseCase {

    private static final String DEFAULT_SORT_BY = "createdAt";
    private static final String DEFAULT_SORT_DIRECTION = "desc";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "paidAt",
            "amount",
            "status"
    );

    private final PaymentRepository paymentRepository;

    public ListCurrentVolunteerPaymentsService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PageResult<PaymentSummaryResult> execute(ListCurrentVolunteerPaymentsQuery query) {
        PaymentSearchCriteria criteria = toSearchCriteria(query);

        return paymentRepository.searchVolunteerPayments(criteria)
                .map(PaymentSummaryResult::from);
    }

    private PaymentSearchCriteria toSearchCriteria(ListCurrentVolunteerPaymentsQuery query) {
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
                "payment"
        );

        return new PaymentSearchCriteria(
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
