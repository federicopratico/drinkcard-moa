package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListPaymentsAdminQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.AdminPaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListPaymentsAdminUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.PaymentSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Service;

import java.util.List;
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
        PageResult<Payment> payments = paymentRepository.searchAdminPayments(criteria);
        List<AdminPaymentSummaryResult> content = payments.content()
                .stream()
                .map(AdminPaymentSummaryResult::from)
                .toList();

        return new PageResult<>(
                content,
                payments.page(),
                payments.size(),
                payments.totalElements(),
                payments.totalPages()
        );
    }

    private PaymentSearchCriteria toSearchCriteria(ListPaymentsAdminQuery query) {
        SortParts sortParts = parseSort(query.sort());

        return new PaymentSearchCriteria(
                parseVolunteerId(query.volunteerId()),
                parseStatus(query.status()),
                query.from(),
                query.to(),
                normalizePage(query.page()),
                normalizeSize(query.size()),
                sortParts.sortBy(),
                sortParts.sortDirection()
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

    private int normalizePage(int page) {
        if (page < 0) {
            return DEFAULT_PAGE;
        }

        return page;
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }

        return Math.min(size, MAX_SIZE);
    }

    private SortParts parseSort(String sort) {
        if (isBlank(sort)) {
            return new SortParts(DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION);
        }

        String[] parts = sort.split(",");
        String sortBy = parts[0].trim();
        String sortDirection = parts.length > 1 ? parts[1].trim().toLowerCase(Locale.ROOT) : DEFAULT_SORT_DIRECTION;

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Unsupported payment sort field: " + sortBy);
        }

        if (!sortDirection.equals("asc") && !sortDirection.equals("desc")) {
            throw new IllegalArgumentException("Unsupported payment sort direction: " + sortDirection);
        }

        return new SortParts(sortBy, sortDirection);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record SortParts(
            String sortBy,
            String sortDirection
    ) {
    }
}
