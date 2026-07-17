package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;

public record UserSummaryResult(
        String userId,
        String fullName,
        String email,
        String role,
        String status,
        DrinkCardAccountSummaryResult drinkCard
) {
    public static UserSummaryResult from(User user, DrinkCardAccountSummaryResult drinkCard) {
        return new UserSummaryResult(
                user.getId().asString(),
                user.getFullName().asString(),
                user.getEmail().asString(),
                user.getRole().name(),
                user.getStatus().name(),
                drinkCard
        );
    }
}
