package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListPaymentsAdminQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.AdminPaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListPaymentsAdminUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.PaymentSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSort;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSortParser;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class ListPaymentsAdminService implements ListPaymentsAdminUseCase {

    private static final String DEFAULT_SORT_BY = "createdAt";
    private static final String DEFAULT_SORT_DIRECTION = "desc";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "paidAt",
            "amount",
            "status",
            "volunteerId"
    );

    private final PaymentRepository paymentRepository;

    public ListPaymentsAdminService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PageResult<AdminPaymentSummaryResult> execute(ListPaymentsAdminQuery query) {
        PaymentSearchCriteria criteria = toSearchCriteria(query);

        return paymentRepository.searchAdminPayments(criteria)
                .map(AdminPaymentSummaryResult::from);
    }

    private PaymentSearchCriteria toSearchCriteria(ListPaymentsAdminQuery query) {
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
        if (isBlank(volunteerId)) {
            return null;
        }

        return VolunteerID.from(volunteerId);
    }

    private PaymentStatus parseStatus(String status) {
        if (isBlank(status)) {
            return null;
        }

        return PaymentStatus.valueOf(status.toUpperCase(Locale.ROOT));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
