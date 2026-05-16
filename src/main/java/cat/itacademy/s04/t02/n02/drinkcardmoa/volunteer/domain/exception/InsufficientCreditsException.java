package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.exception;

public class InsufficientCreditsException extends RuntimeException {
    public InsufficientCreditsException(String message) {
        super(message);
    }
}
