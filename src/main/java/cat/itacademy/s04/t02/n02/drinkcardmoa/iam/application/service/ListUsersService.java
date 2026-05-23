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
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSort;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination.PageSortParser;
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
                "user"
        );

        return new UserSearchCriteria(
                parseRole(query.role()),
                parseStatus(query.status()),
                parseEmail(query.email()),
                pageSort.page(),
                pageSort.size(),
                pageSort.sortBy(),
                pageSort.sortDirection()
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
}
