package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;

import java.util.List;

public interface ListUsersUseCase {
    List<UserSummaryResult> execute();
}
