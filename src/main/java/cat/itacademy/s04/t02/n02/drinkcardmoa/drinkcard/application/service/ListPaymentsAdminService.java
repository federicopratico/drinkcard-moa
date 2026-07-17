package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListPaymentsAdminQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListPaymentsAdminUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.VolunteerDirectory;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.dto.VolunteerProfile;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.PaymentSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSort;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSortParser;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final VolunteerDirectory volunteerDirectory;

    public ListPaymentsAdminService(PaymentRepository paymentRepository, VolunteerDirectory volunteerDirectory) {
        this.paymentRepository = paymentRepository;
        this.volunteerDirectory = volunteerDirectory;
    }

    @Override
    public PageResult<PaymentSummaryResult> execute(ListPaymentsAdminQuery query) {
        PaymentSearchCriteria criteria = toSearchCriteria(query);

        PageResult<Payment> payments = paymentRepository.searchAdminPayments(criteria);

        Set<VolunteerID> ids = payments.content()
                .stream()
                .map(Payment::getVolunteerId)
                .collect(Collectors.toSet());
        Map<VolunteerID, VolunteerProfile> profiles = volunteerDirectory.findAllByIds(ids);

        return payments.map(payment -> PaymentSummaryResult.from(payment, profiles.get(payment.getVolunteerId())));
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
