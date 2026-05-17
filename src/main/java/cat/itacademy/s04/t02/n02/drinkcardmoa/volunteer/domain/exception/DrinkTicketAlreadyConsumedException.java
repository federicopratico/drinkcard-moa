package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.exception;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.exception.DomainException;

public class DrinkTicketAlreadyConsumedException extends DomainException {
    public DrinkTicketAlreadyConsumedException(String message) {
        super(message);
    }
}
