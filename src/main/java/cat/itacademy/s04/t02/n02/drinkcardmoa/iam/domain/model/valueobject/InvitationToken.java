package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.Invitation;

import java.util.Objects;
import java.util.UUID;

public record InvitationToken(String value) {

    public InvitationToken {
        Objects.requireNonNull(value, "InvitationToken cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("InvitationToken cannot be blank");
        }

    }

    public static InvitationToken from(String invitationToken) {
        return new InvitationToken(invitationToken);
    }

    public static InvitationToken generate() {
        return new InvitationToken(UUID.randomUUID().toString());
    }

    public String asString() {
        return value;
    }
}
