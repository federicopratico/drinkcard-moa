package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidEmailException;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {

        Objects.requireNonNull(value, "Email cannot be null");

        value = value.trim().toLowerCase();

        if(!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException("Invalid email format " + value);
        }

    }

    public static Email from(String value) {
        return new Email(value);
    }

    public String asString() {
        return value;
    }
}
