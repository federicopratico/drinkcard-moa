package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.command.AddTurnCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.result.AddTurnResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.usecase.AddTurnUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.TurnRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.exception.TurnAlreadyExistsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.aggregate.Turn;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AddTurnService implements AddTurnUseCase {

    private final TurnRepository turnRepository;

    @Override
    @Transactional
    public AddTurnResult execute(AddTurnCommand cmd) {
        Email email = Email.from(cmd.email());

        if (turnRepository.existsByEmailAndDate(email, cmd.date())) {
            throw new TurnAlreadyExistsException(
                    "A turn already exists for email " + email.asString() + " on date " + cmd.date()
            );
        }

        Turn turn = Turn.create(email, cmd.date(), Instant.now());
        Turn saved = turnRepository.save(turn);

        return new AddTurnResult(
                saved.getTurnId().asString(),
                saved.getEmail().asString(),
                saved.getDate(),
                saved.getCreatedAt()
        );
    }
}
