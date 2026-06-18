package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkCardAccountStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.DomainEvent;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.event.CardPurchasedEvent;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.InsufficientCreditsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.InvalidDrinkCardAccountStatusTransitionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DrinkCardAccountTest {

    @Test
    void create_ShouldCreateDrinkCardAccountWithInitialValues() {
        VolunteerID volunteerId = VolunteerID.generate();

        Instant beforeCreation = Instant.now();
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(volunteerId);
        Instant afterCreation = Instant.now();

        assertAll(
                () -> assertNull(drinkCardAccount.getId()),
                () -> assertEquals(volunteerId, drinkCardAccount.getVolunteerId()),
                () -> assertEquals(0, drinkCardAccount.getCredits()),
                () -> assertNull(drinkCardAccount.getLastPurchaseTimestamp()),
                () -> assertNotNull(drinkCardAccount.getCreatedAt()),
                () -> Assertions.assertEquals(DrinkCardAccountStatus.ACTIVE, drinkCardAccount.getStatus()),
                () -> assertFalse(drinkCardAccount.getCreatedAt().isBefore(beforeCreation)),
                () -> assertFalse(drinkCardAccount.getCreatedAt().isAfter(afterCreation))
        );
    }

    @Test
    void create_WhenVolunteerIdIsNull_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> DrinkCardAccount.create(null));
    }

    @Test
    void rehydrate_ShouldRestoreVolunteerWithGivenValues() {
        VolunteerID volunteerId = VolunteerID.generate();
        Instant lastPurchaseTimestamp = Instant.now();
        Instant createdAt = Instant.now();

        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                volunteerId,
                10,
                lastPurchaseTimestamp,
                createdAt,
                DrinkCardAccountStatus.SUSPENDED
        );

        assertAll(
                () -> assertEquals(1L, drinkCardAccount.getId()),
                () -> assertEquals(volunteerId, drinkCardAccount.getVolunteerId()),
                () -> assertEquals(10, drinkCardAccount.getCredits()),
                () -> assertEquals(lastPurchaseTimestamp, drinkCardAccount.getLastPurchaseTimestamp()),
                () -> assertEquals(createdAt, drinkCardAccount.getCreatedAt()),
                () -> assertEquals(DrinkCardAccountStatus.SUSPENDED, drinkCardAccount.getStatus())
        );
    }

    @Test
    void canPurchaseCard_WhenDrinkCardAccountHasNeverPurchased_ShouldReturnTrue() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(VolunteerID.generate());

        boolean canPurchase = drinkCardAccount.canPurchaseCard(Instant.now());

        assertTrue(canPurchase);
    }

    @Test
    void canCreateTicket_WhenDrinkCardAccountIsActive_ShouldReturnTrue() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(VolunteerID.generate());

        assertTrue(drinkCardAccount.canCreateTicket());
    }

    @Test
    void canCreateTicket_WhenDrinkCardAccountIsSuspended_ShouldReturnFalse() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                VolunteerID.generate(),
                5,
                null,
                Instant.now(),
                DrinkCardAccountStatus.SUSPENDED
        );

        assertFalse(drinkCardAccount.canCreateTicket());
    }

    @Test
    void canCreateTicket_WhenDrinkCardAccountHasRefillDisabled_ShouldReturnTrue() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                VolunteerID.generate(),
                5,
                null,
                Instant.now(),
                DrinkCardAccountStatus.REFILL_DISABLED
        );

        assertTrue(drinkCardAccount.canCreateTicket());
    }

    @Test
    void canRefill_WhenDrinkCardAccountIsActive_ShouldReturnTrue() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(VolunteerID.generate());

        assertTrue(drinkCardAccount.canRefill());
    }

    @Test
    void canRefill_WhenDrinkCardAccountHasRefillDisabled_ShouldReturnFalse() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                VolunteerID.generate(),
                5,
                null,
                Instant.now(),
                DrinkCardAccountStatus.REFILL_DISABLED
        );

        assertFalse(drinkCardAccount.canRefill());
    }

    @Test
    void canRefill_WhenDrinkCardAccountIsSuspended_ShouldReturnFalse() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                VolunteerID.generate(),
                5,
                null,
                Instant.now(),
                DrinkCardAccountStatus.SUSPENDED
        );

        assertFalse(drinkCardAccount.canRefill());
    }

    @Test
    void disableRefill_WhenDrinkCardAccountIsActive_ShouldSetStatusToRefillDisabled() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(VolunteerID.generate());

        drinkCardAccount.disableRefill();

        assertAll(
                () -> assertEquals(DrinkCardAccountStatus.REFILL_DISABLED, drinkCardAccount.getStatus()),
                () -> assertFalse(drinkCardAccount.canRefill()),
                () -> assertTrue(drinkCardAccount.canCreateTicket())
        );
    }

    @Test
    void disableRefill_WhenDrinkCardAccountAlreadyHasRefillDisabled_IsIdempotent() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                VolunteerID.generate(),
                5,
                null,
                Instant.now(),
                DrinkCardAccountStatus.REFILL_DISABLED
        );

        drinkCardAccount.disableRefill();

        assertEquals(DrinkCardAccountStatus.REFILL_DISABLED, drinkCardAccount.getStatus());
    }

    @Test
    void disableRefill_WhenDrinkCardAccountIsSuspended_ShouldThrowInvalidStatusTransition() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                VolunteerID.generate(),
                5,
                null,
                Instant.now(),
                DrinkCardAccountStatus.SUSPENDED
        );

        assertThrows(
                InvalidDrinkCardAccountStatusTransitionException.class,
                drinkCardAccount::disableRefill
        );
        assertEquals(DrinkCardAccountStatus.SUSPENDED, drinkCardAccount.getStatus());
    }

    @Test
    void enableRefill_WhenDrinkCardAccountHasRefillDisabled_ShouldSetStatusToActive() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                VolunteerID.generate(),
                5,
                null,
                Instant.now(),
                DrinkCardAccountStatus.REFILL_DISABLED
        );

        drinkCardAccount.enableRefill();

        assertAll(
                () -> assertEquals(DrinkCardAccountStatus.ACTIVE, drinkCardAccount.getStatus()),
                () -> assertTrue(drinkCardAccount.canRefill())
        );
    }

    @Test
    void enableRefill_WhenDrinkCardAccountIsActive_IsIdempotent() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(VolunteerID.generate());

        drinkCardAccount.enableRefill();

        assertEquals(DrinkCardAccountStatus.ACTIVE, drinkCardAccount.getStatus());
    }

    @Test
    void enableRefill_WhenDrinkCardAccountIsSuspended_ShouldThrowInvalidStatusTransition() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                VolunteerID.generate(),
                5,
                null,
                Instant.now(),
                DrinkCardAccountStatus.SUSPENDED
        );

        assertThrows(
                InvalidDrinkCardAccountStatusTransitionException.class,
                drinkCardAccount::enableRefill
        );
        assertEquals(DrinkCardAccountStatus.SUSPENDED, drinkCardAccount.getStatus());
    }

    @Test
    void canPurchaseCard_WhenDrinkCardAccountAlreadyPurchasedToday_ShouldReturnFalse() {
        Instant now = Instant.now();

        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                VolunteerID.generate(),
                5,
                now.minusSeconds(60),
                now.minusSeconds(3600)
        );

        boolean canPurchase = drinkCardAccount.canPurchaseCard(now);

        assertFalse(canPurchase);
    }

    @Test
    void canPurchaseCard_WhenDrinkCardAccountPurchasedOnPreviousDate_ShouldReturnTrue() {
        Instant now = Instant.now();
        Instant yesterday = LocalDate.now()
                .minusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        DrinkCardAccount drinkCardAccount = DrinkCardAccount.rehydrate(
                1L,
                VolunteerID.generate(),
                5,
                yesterday,
                now.minusSeconds(3600)
        );

        boolean canPurchase = drinkCardAccount.canPurchaseCard(now);

        assertTrue(canPurchase);
    }

    @Test
    void purchaseCard_ShouldAddCardCreditsAndUpdateLastPurchaseTimestamp() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(VolunteerID.generate());
        Card card = Card.newCard();
        Instant purchaseTimestamp = Instant.now();

        drinkCardAccount.purchaseCard(card, purchaseTimestamp);

        assertAll(
                () -> assertEquals(5, drinkCardAccount.getCredits()),
                () -> assertEquals(purchaseTimestamp, drinkCardAccount.getLastPurchaseTimestamp())
        );
    }

    @Test
    void purchaseCard_ShouldRegisterCardPurchasedEvent() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(volunteerId);
        Card card = Card.newCard();
        Instant purchaseTimestamp = Instant.now();

        drinkCardAccount.purchaseCard(card, purchaseTimestamp);

        List<DomainEvent> domainEvents = drinkCardAccount.getDomainEvents();

        assertEquals(1, domainEvents.size());
        assertInstanceOf(CardPurchasedEvent.class, domainEvents.getFirst());

        CardPurchasedEvent event = (CardPurchasedEvent) domainEvents.getFirst();

        assertAll(
                () -> assertEquals(volunteerId.asString(), event.volunteerId()),
                () -> assertEquals(card.getCredits(), event.creditsAdded()),
                () -> assertEquals(card.getPrice(), event.paidAmount()),
                () -> assertEquals(purchaseTimestamp, event.occurredOn())
        );
    }

    @Test
    void getDomainEvents_ShouldReturnEventsAndClearThem() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(VolunteerID.generate());

        drinkCardAccount.purchaseCard(Card.newCard(), Instant.now());

        List<DomainEvent> firstCall = drinkCardAccount.getDomainEvents();
        List<DomainEvent> secondCall = drinkCardAccount.getDomainEvents();

        assertAll(
                () -> assertEquals(1, firstCall.size()),
                () -> assertTrue(secondCall.isEmpty())
        );
    }

    @Test
    void purchaseCard_WhenCardIsNull_ShouldThrowNullPointerException() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(VolunteerID.generate());

        assertThrows(
                NullPointerException.class,
                () -> drinkCardAccount.purchaseCard(null, Instant.now())
        );
    }

    @Test
    void purchaseCard_WhenPurchaseTimestampIsNull_ShouldThrowNullPointerException() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(VolunteerID.generate());

        assertThrows(
                NullPointerException.class,
                () -> drinkCardAccount.purchaseCard(Card.newCard(), null)
        );
    }

    @Test
    void canConsumeCredits_WhenDrinkCardAccountHasCredits_ShouldReturnTrue() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(VolunteerID.generate());
        Card card = Card.newCard();

        drinkCardAccount.purchaseCard(card, Instant.now());

        assertTrue(drinkCardAccount.canConsumeCredit());
    }

    @Test
    void canConsumeCredits_WhenDrinkCardAccountHasNoCredits_ShouldReturnFalse() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(VolunteerID.generate());

        assertFalse(drinkCardAccount.canConsumeCredit());
    }

    @Test
    void consumeCredits_WhenDrinkCardAccountHasCredits_ShouldDecreaseCredits() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(VolunteerID.generate());
        Card card = Card.newCard();

        drinkCardAccount.purchaseCard(card, Instant.now());

        assertEquals(5, drinkCardAccount.getCredits());
        drinkCardAccount.consumeCredit();
        assertEquals(4, drinkCardAccount.getCredits());
    }

    @Test
    void consumeCredits_WhenDrinkCardAccountHasNoCredits_ThrowInsufficientCreditsException() {
        DrinkCardAccount drinkCardAccount = DrinkCardAccount.create(VolunteerID.generate());

        assertThrows(
                InsufficientCreditsException.class,
                drinkCardAccount::consumeCredit
        );
    }
}
