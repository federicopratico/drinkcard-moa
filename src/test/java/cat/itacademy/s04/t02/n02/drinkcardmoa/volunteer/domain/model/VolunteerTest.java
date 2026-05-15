package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.event.CardPurchasedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VolunteerTest {

    @Test
    void create_ShouldCreateVolunteerWithInitialValues() {
        VolunteerID volunteerId = VolunteerID.generate();

        Instant beforeCreation = Instant.now();
        Volunteer volunteer = Volunteer.create(volunteerId);
        Instant afterCreation = Instant.now();

        assertAll(
                () -> assertNull(volunteer.getId()),
                () -> assertEquals(volunteerId, volunteer.getVolunteerId()),
                () -> assertEquals(0, volunteer.getCredits()),
                () -> assertNull(volunteer.getLastPurchaseTimestamp()),
                () -> assertNotNull(volunteer.getCreatedAt()),
                () -> assertFalse(volunteer.getCreatedAt().isBefore(beforeCreation)),
                () -> assertFalse(volunteer.getCreatedAt().isAfter(afterCreation))
        );
    }

    @Test
    void create_WhenVolunteerIdIsNull_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> Volunteer.create(null));
    }

    @Test
    void rehydrate_ShouldRestoreVolunteerWithGivenValues() {
        VolunteerID volunteerId = VolunteerID.generate();
        Instant lastPurchaseTimestamp = Instant.now();
        Instant createdAt = Instant.now();

        Volunteer volunteer = Volunteer.rehydrate(
                1L,
                volunteerId,
                10,
                lastPurchaseTimestamp,
                createdAt
        );

        assertAll(
                () -> assertEquals(1L, volunteer.getId()),
                () -> assertEquals(volunteerId, volunteer.getVolunteerId()),
                () -> assertEquals(10, volunteer.getCredits()),
                () -> assertEquals(lastPurchaseTimestamp, volunteer.getLastPurchaseTimestamp()),
                () -> assertEquals(createdAt, volunteer.getCreatedAt())
        );
    }

    @Test
    void canPurchaseCard_WhenVolunteerHasNeverPurchased_ShouldReturnTrue() {
        Volunteer volunteer = Volunteer.create(VolunteerID.generate());

        boolean canPurchase = volunteer.canPurchaseCard(Instant.now());

        assertTrue(canPurchase);
    }

    @Test
    void canPurchaseCard_WhenVolunteerAlreadyPurchasedToday_ShouldReturnFalse() {
        Instant now = Instant.now();

        Volunteer volunteer = Volunteer.rehydrate(
                1L,
                VolunteerID.generate(),
                5,
                now.minusSeconds(60),
                now.minusSeconds(3600)
        );

        boolean canPurchase = volunteer.canPurchaseCard(now);

        assertFalse(canPurchase);
    }

    @Test
    void canPurchaseCard_WhenVolunteerPurchasedOnPreviousDate_ShouldReturnTrue() {
        Instant now = Instant.now();
        Instant yesterday = LocalDate.now()
                .minusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        Volunteer volunteer = Volunteer.rehydrate(
                1L,
                VolunteerID.generate(),
                5,
                yesterday,
                now.minusSeconds(3600)
        );

        boolean canPurchase = volunteer.canPurchaseCard(now);

        assertTrue(canPurchase);
    }

    @Test
    void purchaseCard_ShouldAddCardCreditsAndUpdateLastPurchaseTimestamp() {
        Volunteer volunteer = Volunteer.create(VolunteerID.generate());
        Card card = Card.newCard();
        Instant purchaseTimestamp = Instant.now();

        volunteer.purchaseCard(card, purchaseTimestamp);

        assertAll(
                () -> assertEquals(5, volunteer.getCredits()),
                () -> assertEquals(purchaseTimestamp, volunteer.getLastPurchaseTimestamp())
        );
    }

    @Test
    void purchaseCard_ShouldRegisterCardPurchasedEvent() {
        VolunteerID volunteerId = VolunteerID.generate();
        Volunteer volunteer = Volunteer.create(volunteerId);
        Card card = Card.newCard();
        Instant purchaseTimestamp = Instant.now();

        volunteer.purchaseCard(card, purchaseTimestamp);

        List<Object> domainEvents = volunteer.getDomainEvents();

        assertEquals(1, domainEvents.size());
        assertInstanceOf(CardPurchasedEvent.class, domainEvents.getFirst());

        CardPurchasedEvent event = (CardPurchasedEvent) domainEvents.getFirst();

        assertAll(
                () -> assertEquals(volunteerId.asString(), event.volunteerId()),
                () -> assertEquals(card.getCredits(), event.creditsAdded()),
                () -> assertEquals(card.getPrice(), event.paidAmount()),
                () -> assertEquals(purchaseTimestamp, event.timestamp())
        );
    }

    @Test
    void getDomainEvents_ShouldReturnEventsAndClearThem() {
        Volunteer volunteer = Volunteer.create(VolunteerID.generate());

        volunteer.purchaseCard(Card.newCard(), Instant.now());

        List<Object> firstCall = volunteer.getDomainEvents();
        List<Object> secondCall = volunteer.getDomainEvents();

        assertAll(
                () -> assertEquals(1, firstCall.size()),
                () -> assertTrue(secondCall.isEmpty())
        );
    }

    @Test
    void purchaseCard_WhenCardIsNull_ShouldThrowNullPointerException() {
        Volunteer volunteer = Volunteer.create(VolunteerID.generate());

        assertThrows(
                NullPointerException.class,
                () -> volunteer.purchaseCard(null, Instant.now())
        );
    }

    @Test
    void purchaseCard_WhenPurchaseTimestampIsNull_ShouldThrowNullPointerException() {
        Volunteer volunteer = Volunteer.create(VolunteerID.generate());

        assertThrows(
                NullPointerException.class,
                () -> volunteer.purchaseCard(Card.newCard(), null)
        );
    }
}