package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public record RefreshTokenFamilyID(UUID value) {
    public RefreshTokenFamilyID {
        Objects.requireNonNull(value, "RefreshTokenFamilyID value cannot be null");
    }

    public static RefreshTokenFamilyID generate() {
        return new RefreshTokenFamilyID(UUID.randomUUID());
    }

    public static RefreshTokenFamilyID from(String uuidString) {
        return new RefreshTokenFamilyID(UUID.fromString(uuidString));
    }

    public String asString() {
        return value.toString();
    }
}
