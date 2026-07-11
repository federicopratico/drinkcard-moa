package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.aggregate;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.valueobject.TurnID;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public class Turn {

    private final TurnID turnId;
    private final Email email;
    private final LocalDate date;
    private final Instant createdAt;

    private Turn(TurnID turnId, Email email, LocalDate date, Instant createdAt) {
        this.turnId = Objects.requireNonNull(turnId);
        this.email = Objects.requireNonNull(email);
        this.date = Objects.requireNonNull(date);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Turn create(Email email, LocalDate date, Instant now) {
        return new Turn(TurnID.generate(), email, date, now);
    }

    public static Turn rehydrate(TurnID turnId, Email email, LocalDate date, Instant createdAt) {
        return new Turn(turnId, email, date, createdAt);
    }

    public TurnID getTurnId() {
        return turnId;
    }

    public Email getEmail() {
        return email;
    }

    public LocalDate getDate() {
        return date;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
