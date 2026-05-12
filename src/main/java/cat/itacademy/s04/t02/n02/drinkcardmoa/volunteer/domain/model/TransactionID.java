package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record TransactionID(UUID value) implements Serializable {
    public TransactionID {
        Objects.requireNonNull(value, "TransactionID value cannot be null");
    }

    public static TransactionID generate() {
        return new TransactionID(UUID.randomUUID());
    }

    public static TransactionID from(String uuidString) {
        return new TransactionID(UUID.fromString(uuidString));
    }

    public String asString() {
        return value.toString();
    }
}
