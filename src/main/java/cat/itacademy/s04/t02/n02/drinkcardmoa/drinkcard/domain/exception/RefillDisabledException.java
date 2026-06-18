package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.exception.DomainException;

public class RefillDisabledException extends DomainException {
    public RefillDisabledException(String message) {
        super(message);
    }
}
