package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command.SendInvitationEmailCommand;

public interface SendInvitationEmailUseCase {
    boolean execute(SendInvitationEmailCommand cmd);
}
