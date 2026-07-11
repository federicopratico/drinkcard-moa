package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.command.AddTurnCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.result.AddTurnResult;

public interface AddTurnUseCase {
    AddTurnResult execute(AddTurnCommand cmd);
}
