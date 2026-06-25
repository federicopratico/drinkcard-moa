package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record PasswordResetID(UUID value) implements Serializable {
    public PasswordResetID {
        Objects.requireNonNull(value, "PasswordResetID value cannot be null");
    }

    public static PasswordResetID generate() {
        return new PasswordResetID(UUID.randomUUID());
    }

    public static PasswordResetID from(String uuidString) {
        return new PasswordResetID(UUID.fromString(uuidString));
    }

    public String asString() {
        return value.toString();
    }
}
