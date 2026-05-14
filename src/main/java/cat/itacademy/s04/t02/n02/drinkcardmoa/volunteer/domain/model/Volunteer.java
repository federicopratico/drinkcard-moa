package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.event.CardPurchasedEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Volunteer {

    private Long id;
    private VolunteerID volunteerId;
    private int credits;
    private Instant lastPurchaseTimestamp;
    private Instant createdAt;

    private final List<Object> domainEvents = new ArrayList<>();

    private Volunteer(Long id, VolunteerID volunteerID, int credits, Instant lastPurchaseTimestamp, Instant createdAt) {
        this.id = id;
        this.volunteerId = Objects.requireNonNull(volunteerID);
        this. credits = credits;
        this.lastPurchaseTimestamp = lastPurchaseTimestamp;
        this.createdAt = createdAt;
    }

    public static Volunteer create(VolunteerID volunteerID) {
        return new Volunteer(
                null,
                volunteerID,
                0,
                null,
                Instant.now()
        );
    }

    public static Volunteer rehydrate(Long id, VolunteerID volunteerID, int credits, Instant lastPurchaseTimestamp, Instant createdAt) {
        return new Volunteer(
                id,
                volunteerID,
                credits,
                lastPurchaseTimestamp,
                createdAt
        );
    }

    public boolean canPurchaseCard(Instant now) {
        return !hasPurchasedInLast24Hours(now);
    }

    public void purchaseCard(Card card, Instant purchaseTimestamp) {
        Objects.requireNonNull(card, "Card cannot be null");
        Objects.requireNonNull(purchaseTimestamp, "Timestamp cannot be null");

        credits += card.getCredits();
        lastPurchaseTimestamp = purchaseTimestamp;

        registerEvent(new CardPurchasedEvent(
                volunteerId.asString(),
                card.getCredits(),
                card.getPrice(),
                purchaseTimestamp
        ));
    }

    public Long getId() {
        return id;
    }

    public VolunteerID getVolunteerId() {
        return volunteerId;
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
    public List<Object> getDomainEvents() {
        List<Object> eventsToFire = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return eventsToFire;
    }

    private boolean hasPurchasedInLast24Hours(Instant now) {
        if(lastPurchaseTimestamp == null)
            return false;

        LocalDate lastPurchaseDate = lastPurchaseTimestamp
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate currentDate = now
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return lastPurchaseDate.equals(currentDate);
    }

    private void registerEvent(Object event) {
        domainEvents.add(event);
    }
}
