package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record InvitationID(UUID value) implements Serializable {

    public InvitationID {
        Objects.requireNonNull(value, "VolunteerID value cannot be null");
    }

    public static InvitationID generate() {
        return new InvitationID(UUID.randomUUID());
    }

    public static InvitationID from(String uuidString) {
        return new InvitationID(UUID.fromString(uuidString));
    }

    public String asString() {
        return value.toString();
    }
}
