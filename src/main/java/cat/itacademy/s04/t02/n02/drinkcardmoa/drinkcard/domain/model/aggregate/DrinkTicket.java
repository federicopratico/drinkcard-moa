package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkTicketID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkTicketStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkType;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkTicketExpiredException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.InvalidDrinkTicketStateException;

import java.time.Instant;
import java.util.Objects;

public class DrinkTicket {

    private static final int DEFAULT_EXPIRATION_SECONDS = 90;

    private DrinkTicketID drinkTicketId;
    private VolunteerID volunteerId;
    private DrinkType drinkType;
    private DrinkTicketStatus status;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant consumedAt;
    private String consumedByStaffId;

    private DrinkTicket(DrinkTicketID drinkTicketId, VolunteerID volunteerId, DrinkType drinkType, DrinkTicketStatus status, Instant createdAt, Instant expiresAt, Instant consumedAt, String consumedByStaffId) {
        this.drinkTicketId = Objects.requireNonNull(drinkTicketId);
        this.volunteerId = Objects.requireNonNull(volunteerId);
        this.drinkType = Objects.requireNonNull(drinkType);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.consumedAt = consumedAt;
        this.consumedByStaffId = consumedByStaffId;
    }

    public static DrinkTicket pending(VolunteerID volunteerId, DrinkType drinkType, Instant now) {
        Instant expiresAt = now.plusSeconds(DEFAULT_EXPIRATION_SECONDS);
        return new DrinkTicket(
                DrinkTicketID.generate(),
                volunteerId,
                drinkType,
                DrinkTicketStatus.PENDING,
                now,
                expiresAt,
                null,
                null
        );
    }

    public static DrinkTicket rehydrate(DrinkTicketID drinkTicketId, VolunteerID volunteerId, DrinkType drinkType, DrinkTicketStatus status, Instant createdAt, Instant expiresAt, Instant consumedAt, String consumedByStaffId) {
        return new DrinkTicket(drinkTicketId, volunteerId, drinkType, status, createdAt, expiresAt, consumedAt, consumedByStaffId);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isConsumed() {
        return status == DrinkTicketStatus.CONSUMED;
    }

    public void markAsExpired() {
        if(status != DrinkTicketStatus.PENDING) {
            throw new InvalidDrinkTicketStateException("DrinkTicket is not in pending state");
        }

        this.status = DrinkTicketStatus.EXPIRED;
    }

    public void consume(String staffId, Instant consumedAt) {
        if(isExpired(Instant.now())) {
            throw new DrinkTicketExpiredException("DrinkTicket has expired");
        }

        if(status != DrinkTicketStatus.PENDING) {
            throw new InvalidDrinkTicketStateException("DrinkTicket is not in pending state");
        }

        this.status = DrinkTicketStatus.CONSUMED;
        this.consumedAt = consumedAt;
        this.consumedByStaffId = staffId;
    }

    public DrinkTicketID getDrinkTicketId() {
        return drinkTicketId;
    }
    public VolunteerID getVolunteerId() {
        return volunteerId;
    }
    public DrinkType getDrinkType() {
        return drinkType;
    }
    public DrinkTicketStatus getStatus() {
        return status;
    }
    public Instant getExpiresAt() {
        return expiresAt;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getConsumedAt() {
        return consumedAt;
    }
    public String getConsumedByStaffId() {
        return consumedByStaffId;
    }
}
