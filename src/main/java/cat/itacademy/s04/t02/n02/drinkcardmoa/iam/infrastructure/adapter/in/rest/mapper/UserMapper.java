package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.CurrentUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.CurrentUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.CurrentUserResponse;
import org.springframework.stereotype.Component;

@Component("userRestMapper")
public class UserMapper {

    public CurrentUserCommand toCommand(String email) {
        return new CurrentUserCommand(email);
    }

    public CurrentUserResponse toResponse(CurrentUserResult result) {
        return new CurrentUserResponse(
                result.userId(),
                result.fullName(),
                result.email(),
                result.role(),
                result.status()
        );
    }
}
