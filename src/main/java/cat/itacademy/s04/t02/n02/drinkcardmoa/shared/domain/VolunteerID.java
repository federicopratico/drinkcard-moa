package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record VolunteerID(UUID value) implements Serializable {

    public VolunteerID {
        Objects.requireNonNull(value, "VolunteerID value cannot be null");
    }

    public static VolunteerID generate() {
        return new VolunteerID(UUID.randomUUID());
    }

    public static VolunteerID from(String uuidString) {
        return new VolunteerID(UUID.fromString(uuidString));
    }

    public String asString() {
        return value.toString();
    }
}
