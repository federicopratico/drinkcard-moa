package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.exception.DomainException;

public class DrinkCardAccountSuspendedException extends DomainException {
    public DrinkCardAccountSuspendedException(String message) {
        super(message);
    }
}
