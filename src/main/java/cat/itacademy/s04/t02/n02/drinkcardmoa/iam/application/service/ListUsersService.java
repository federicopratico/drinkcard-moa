package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.ListUsersQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.ListUsersUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.query.UserSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class ListUsersService implements ListUsersUseCase {

    private static final String DEFAULT_SORT_BY = "email";
    private static final String DEFAULT_SORT_DIRECTION = "asc";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "email",
            "firstName",
            "lastName",
            "role",
            "status"
    );

    private final UserRepository userRepository;

    public ListUsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public PageResult<UserSummaryResult> execute(ListUsersQuery query) {

        UserSearchCriteria criteria = toSearchCriteria(query);

        return userRepository.searchUsers(criteria)
                .map(UserSummaryResult::from);
    }

    private UserSearchCriteria toSearchCriteria(ListUsersQuery query) {
        SortParts sortParts = parseSort(query.sort());

        return new UserSearchCriteria(
                parseRole(query.role()),
                parseStatus(query.status()),
                parseEmail(query.email()),
                normalizePage(query.page()),
                normalizeSize(query.size()),
                sortParts.sortBy(),
                sortParts.sortDirection()
        );
    }

    private Role parseRole(String role) {
        return role == null || role.isBlank()
                ? null
                : Role.valueOf(role.toUpperCase());
    }

    private UserStatus parseStatus(String status) {
        return status == null || status.isBlank()
                ? null
                : UserStatus.valueOf(status.toUpperCase());
    }

    private Email parseEmail(String email) {
        return email == null || email.isBlank()
                ? null
                : Email.from(email);
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
        if (sort == null || sort.isBlank()) {
            return new SortParts(DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION);
        }

        String[] parts = sort.split(",");
        String sortBy = parts[0].trim();
        String sortDirection = parts.length > 1 ? parts[1].trim().toLowerCase(Locale.ROOT) : DEFAULT_SORT_DIRECTION;

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Unsupported user sort field: " + sortBy);
        }

        if (!sortDirection.equals("asc") && !sortDirection.equals("desc")) {
            throw new IllegalArgumentException("Unsupported user sort direction: " + sortDirection);
        }

        return new SortParts(sortBy, sortDirection);
    }

    private record SortParts(
            String sortBy,
            String sortDirection
    ) {
    }
}
