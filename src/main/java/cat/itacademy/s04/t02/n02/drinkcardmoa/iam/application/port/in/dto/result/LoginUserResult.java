package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;

public record LoginUserResult(
        String token,
        String volunteerId,
        String email,
        String role
)
{
    public static LoginUserResult from(User user, String token) {
        return new LoginUserResult(token, user.getId().asString() , user.getEmail().asString(), user.getRole().name());
    }
}
