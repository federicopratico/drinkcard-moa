package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.exception;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.exception.DomainException;

public class VolunteerNotFoundException extends DomainException {
    public VolunteerNotFoundException(String message) {
        super(message);
    }
}
