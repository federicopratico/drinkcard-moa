package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.ListUsersQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;

public interface ListUsersUseCase {
    PageResult<UserSummaryResult> execute(ListUsersQuery query);
}
