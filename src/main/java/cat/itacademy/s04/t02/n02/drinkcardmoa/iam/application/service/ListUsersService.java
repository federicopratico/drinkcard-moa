package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.ListUsersQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.ListUsersUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListUsersService implements ListUsersUseCase {

    private final UserRepository userRepository;

    public ListUsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserSummaryResult> execute(ListUsersQuery query) {
        Role role = parseRole(query.role());
        UserStatus status = parseStatus(query.status());
        Email email = parseEmail(query.email());


        return userRepository.findAllByFilters(role, status, email)
                .stream()
                .map(UserSummaryResult::from)
                .toList();
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
