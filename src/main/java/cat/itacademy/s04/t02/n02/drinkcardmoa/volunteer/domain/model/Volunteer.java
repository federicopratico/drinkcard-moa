package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.DomainEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Volunteer {

    private long id;
    private VolunteerID volunteerID;
    private int credits;
    private Instant lastPurchaseTimestamp;
    private Instant createdAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Volunteer(long id, VolunteerID volunteerID, int credits, Instant lastPurchaseTimestamp, Instant createdAt) {
        this.id = id;
        this.volunteerID = Objects.requireNonNull(volunteerID);
        this. credits = credits;
        this.lastPurchaseTimestamp = lastPurchaseTimestamp;
        this.createdAt = createdAt;
    }

    public static Volunteer create(VolunteerID volunteerID) {
        return new Volunteer(
                0,
                volunteerID,
                0,
                null,
                Instant.now()
        );
    }

    public static Volunteer rehydrate(long id, VolunteerID volunteerID, int credits, Instant lastPurchaseTimestamp, Instant createdAt) {
        return new Volunteer(
                id,
                volunteerID,
                credits,
                lastPurchaseTimestamp,
                createdAt
        );
    }

    public long getId() {
        return id;
    }
    public VolunteerID getVolunteerID() {
        return volunteerID;
    }
    public int getCredits() {
        return credits;
    }
    public Instant getLastPurchaseTimestamp() {
        return lastPurchaseTimestamp;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public List<DomainEvent> getDomainEvents() {
        List<DomainEvent> eventsToFire = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return eventsToFire;
    }
}
