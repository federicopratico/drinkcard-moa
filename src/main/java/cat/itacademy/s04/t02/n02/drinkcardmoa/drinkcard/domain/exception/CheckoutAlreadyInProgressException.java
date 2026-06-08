package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.exception.DomainException;

public class CheckoutAlreadyInProgressException extends DomainException {
    public CheckoutAlreadyInProgressException(String string) {
        super(string);
    }
}
