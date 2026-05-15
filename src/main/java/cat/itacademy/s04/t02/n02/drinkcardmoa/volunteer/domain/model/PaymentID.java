package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record PaymentID(UUID value) implements Serializable {
    public PaymentID {
        Objects.requireNonNull(value, "PaymentID value cannot be null");
    }

    public static PaymentID generate() {
        return new PaymentID(UUID.randomUUID());
    }

    public static PaymentID from(String uuidString) {
        return new PaymentID(UUID.fromString(uuidString));
    }

    public String asString() {
        return value.toString();
    }
}
