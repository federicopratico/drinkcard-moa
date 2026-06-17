package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.exception.DomainException;

public class CannotDeleteAdminException extends DomainException {
    public CannotDeleteAdminException(String message) {
        super(message);
    }
}
