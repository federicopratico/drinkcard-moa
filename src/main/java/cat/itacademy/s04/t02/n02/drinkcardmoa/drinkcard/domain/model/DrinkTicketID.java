package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record DrinkTicketID(UUID value) implements Serializable {
    public DrinkTicketID {
        Objects.requireNonNull(value, "DrinkTickerID value cannot be null");
    }

    public static DrinkTicketID generate() {
        return new DrinkTicketID(UUID.randomUUID());
    }

    public static DrinkTicketID from(String uuidString) {
        return new DrinkTicketID(UUID.fromString(uuidString));
    }

    public String asString() {
        return value.toString();
    }
}
