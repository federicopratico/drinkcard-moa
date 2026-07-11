package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.command.DeleteTurnCommand;

public interface DeleteTurnUseCase {
    void execute(DeleteTurnCommand cmd);
}
