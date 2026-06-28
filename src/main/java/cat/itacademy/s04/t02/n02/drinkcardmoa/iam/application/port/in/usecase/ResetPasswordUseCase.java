package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.ResetPasswordCommand;

public interface ResetPasswordUseCase {
    void execute(ResetPasswordCommand cmd);
}
