package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.exception;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.exception.DomainException;

public class TurnNotFoundException extends DomainException {
    public TurnNotFoundException(String message) {
        super(message);
    }
}
