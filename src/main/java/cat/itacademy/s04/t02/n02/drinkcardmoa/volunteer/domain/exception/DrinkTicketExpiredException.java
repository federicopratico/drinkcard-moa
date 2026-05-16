package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.exception;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.exception.DomainException;

public class DrinkTicketExpiredException extends DomainException {
    public DrinkTicketExpiredException(String message) {
        super(message);
    }
}
