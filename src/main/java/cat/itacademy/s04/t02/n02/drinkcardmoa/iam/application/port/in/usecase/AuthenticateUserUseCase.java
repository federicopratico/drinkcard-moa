package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.LoginUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.LoginUserResult;

public interface AuthenticateUserUseCase {
    LoginUserResult execute(LoginUserCommand cmd);
}
