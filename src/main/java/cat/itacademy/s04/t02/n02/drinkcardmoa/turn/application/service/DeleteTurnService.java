package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.command.DeleteTurnCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.usecase.DeleteTurnUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.TurnRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.exception.TurnNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.valueobject.TurnID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteTurnService implements DeleteTurnUseCase {

    private final TurnRepository turnRepository;

    @Override
    @Transactional
    public void execute(DeleteTurnCommand cmd) {
        TurnID turnId;
        try {
            turnId = TurnID.from(cmd.turnId());
        } catch (IllegalArgumentException e) {
            throw new TurnNotFoundException("Turn not found with id: " + cmd.turnId());
        }

        if (turnRepository.findById(turnId).isEmpty()) {
            throw new TurnNotFoundException("Turn not found with id: " + cmd.turnId());
        }

        turnRepository.deleteById(turnId);
    }
}
