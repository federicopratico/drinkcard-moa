package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;

public record RegisterUserResult(
        String id,
        String firstName,
        String lastName,
        String email
)
{
    public static RegisterUserResult from(User user) {
        return new RegisterUserResult(
                user.getId().asString(),
                user.getFullName().firstName(),
                user.getFullName().lastName(),
                user.getEmail().asString()
        );
    }
}
