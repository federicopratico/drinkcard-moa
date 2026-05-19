package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.query;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;

public record UserSearchCriteria(
        Role role,
        UserStatus status,
        Email email,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
}
