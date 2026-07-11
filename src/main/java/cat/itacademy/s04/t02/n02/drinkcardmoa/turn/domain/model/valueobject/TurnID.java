package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record TurnID(UUID value) implements Serializable {

    public TurnID {
        Objects.requireNonNull(value, "TurnID value cannot be null");
    }

    public static TurnID generate() {
        return new TurnID(UUID.randomUUID());
    }

    public static TurnID from(String uuidString) {
        return new TurnID(UUID.fromString(uuidString));
    }

    public String asString() {
        return value.toString();
    }
}
