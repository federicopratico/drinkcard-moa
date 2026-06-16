package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record RefreshTokenID(UUID value) {
    public RefreshTokenID {
        Objects.requireNonNull(value, "RefreshTokenID value cannot be null");
    }

    public static RefreshTokenID generate() {
        return new RefreshTokenID(UUID.randomUUID());
    }

    public static RefreshTokenID from(String uuidString) {
        return new RefreshTokenID(UUID.fromString(uuidString));
    }

    public String asString() {
        return value.toString();
    }
}
