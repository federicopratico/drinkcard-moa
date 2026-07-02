package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command.SendResetPasswordEmailCommand;

public interface SendPasswordResetEmailUseCase {
    boolean execute(SendResetPasswordEmailCommand cmd);
}
