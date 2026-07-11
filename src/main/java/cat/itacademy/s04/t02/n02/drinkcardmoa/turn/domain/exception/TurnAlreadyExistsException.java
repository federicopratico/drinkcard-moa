package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.exception;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.exception.DomainException;

public class TurnAlreadyExistsException extends DomainException {
    public TurnAlreadyExistsException(String message) {
        super(message);
    }
}
