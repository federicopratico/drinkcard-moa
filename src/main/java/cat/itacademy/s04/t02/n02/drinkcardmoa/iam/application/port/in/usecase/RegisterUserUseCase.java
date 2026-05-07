package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.RegisterUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RegisterUserResult;

public interface RegisterUserUseCase {
    RegisterUserResult execute(RegisterUserCommand cmd);
}
