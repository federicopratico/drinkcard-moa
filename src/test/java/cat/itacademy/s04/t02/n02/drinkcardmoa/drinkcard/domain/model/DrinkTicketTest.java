package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkTicketExpiredException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.InvalidDrinkTicketStateException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DrinkTicketTest {

    @Test
    void pending_ShouldCreatePendingDrinkTicketWithInitialValues() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkType drinkType = DrinkType.BEER;

        Instant createdAt = Instant.now();
        DrinkTicket ticket = DrinkTicket.pending(volunteerId, drinkType, createdAt);

        assertAll(
                () -> assertNotNull(ticket.getDrinkTicketId()),
                () -> assertEquals(volunteerId, ticket.getVolunteerId()),
                () -> assertEquals(drinkType, ticket.getDrinkType()),
                () -> assertEquals(DrinkTicketStatus.PENDING, ticket.getStatus()),
                () -> assertEquals(createdAt, ticket.getCreatedAt()),
                () -> assertEquals(createdAt.plusSeconds(90), ticket.getExpiresAt()),
                () -> assertNull(ticket.getConsumedAt()),
                () -> assertNull(ticket.getConsumedByStaffId())
        );
    }

    @Test
    void rehydrate_ShouldRestoreDrinkTicketWithGivenValues() {
        DrinkTicketID ticketId = DrinkTicketID.generate();
        VolunteerID volunteerId = VolunteerID.generate();
        Instant createdAt = Instant.now().minusSeconds(120);
        Instant expiresAt = createdAt.plusSeconds(90);
        Instant consumedAt = createdAt.plusSeconds(30);
        String staffId = "staff-123";

        DrinkTicket ticket = DrinkTicket.rehydrate(
                ticketId,
                volunteerId,
                DrinkType.WATER,
                DrinkTicketStatus.CONSUMED,
                createdAt,
                expiresAt,
                consumedAt,
                staffId
        );

        assertAll(
                () -> assertEquals(ticketId, ticket.getDrinkTicketId()),
                () -> assertEquals(volunteerId, ticket.getVolunteerId()),
                () -> assertEquals(DrinkType.WATER, ticket.getDrinkType()),
                () -> assertEquals(DrinkTicketStatus.CONSUMED, ticket.getStatus()),
                () -> assertEquals(expiresAt, ticket.getExpiresAt()),
                () -> assertEquals(createdAt, ticket.getCreatedAt()),
                () -> assertEquals(consumedAt, ticket.getConsumedAt()),
                () -> assertEquals(staffId, ticket.getConsumedByStaffId())
        );
    }

    @Test
    void isExpired_WhenNowIsAfterExpiration_ShouldReturnTrue() {
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(90);

        DrinkTicket ticket = DrinkTicket.rehydrate(
                DrinkTicketID.generate(),
                VolunteerID.generate(),
                DrinkType.BEER,
                DrinkTicketStatus.PENDING,
                expiresAt,
                createdAt,
                null,
                null
        );

        assertTrue(ticket.isExpired(expiresAt.plusSeconds(1)));
    }

    @Test
    void isExpired_WhenNowIsBeforeExpiration_ShouldReturnFalse() {
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(90);

        DrinkTicket ticket = DrinkTicket.rehydrate(
                DrinkTicketID.generate(),
                VolunteerID.generate(),
                DrinkType.BEER,
                DrinkTicketStatus.PENDING,
                createdAt,
                expiresAt,
                null,
                null
        );

        assertFalse(ticket.isExpired(expiresAt.minusSeconds(1)));
    }

    @Test
    void isExpired_WhenNowEqualsExpiration_ShouldReturnFalse() {
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(90);

        DrinkTicket ticket = DrinkTicket.rehydrate(
                DrinkTicketID.generate(),
                VolunteerID.generate(),
                DrinkType.BEER,
                DrinkTicketStatus.PENDING,
                createdAt,
                expiresAt,
                null,
                null
        );

        assertFalse(ticket.isExpired(expiresAt));
    }

    @Test
    void isConsumed_WhenStatusIsPending_ShouldReturnFalse() {
        DrinkTicket ticket = DrinkTicket.pending(VolunteerID.generate(), DrinkType.BEER, Instant.now());

        assertFalse(ticket.isConsumed());
    }

    @Test
    void isConsumed_WhenStatusIsConsumed_ShouldReturnTrue() {
        DrinkTicket ticket = DrinkTicket.rehydrate(
                DrinkTicketID.generate(),
                VolunteerID.generate(),
                DrinkType.BEER,
                DrinkTicketStatus.CONSUMED,
                Instant.now().plusSeconds(90),
                Instant.now(),
                Instant.now(),
                "staff-123"
        );

        assertTrue(ticket.isConsumed());
    }

    @Test
    void markAsExpired_WhenTicketIsPending_ShouldMarkTicketAsExpired() {
        DrinkTicket ticket = DrinkTicket.pending(VolunteerID.generate(), DrinkType.BEER, Instant.now());

        ticket.markAsExpired();

        assertEquals(DrinkTicketStatus.EXPIRED, ticket.getStatus());
    }

    @Test
    void markAsExpired_WhenTicketIsConsumed_ShouldThrowInvalidDrinkTicketStateException() {
        DrinkTicket ticket = DrinkTicket.pending(VolunteerID.generate(), DrinkType.BEER, Instant.now());
        ticket.consume("staff-123", Instant.now());

        assertThrows(
                InvalidDrinkTicketStateException.class,
                ticket::markAsExpired
        );
    }

    @Test
    void markAsExpired_WhenTicketIsAlreadyExpired_ShouldThrowInvalidDrinkTicketStateException() {
        DrinkTicket ticket = DrinkTicket.rehydrate(
                DrinkTicketID.generate(),
                VolunteerID.generate(),
                DrinkType.BEER,
                DrinkTicketStatus.EXPIRED,
                Instant.now().minusSeconds(1),
                Instant.now().minusSeconds(120),
                null,
                null
        );

        assertThrows(
                InvalidDrinkTicketStateException.class,
                ticket::markAsExpired
        );
    }

    @Test
    void consume_WhenTicketIsPending_ShouldMarkTicketAsConsumed() {
        DrinkTicket ticket = DrinkTicket.pending(VolunteerID.generate(), DrinkType.BEER, Instant.now());
        Instant consumedAt = Instant.now();
        String staffId = "staff-123";

        ticket.consume(staffId, consumedAt);

        assertAll(
                () -> assertEquals(DrinkTicketStatus.CONSUMED, ticket.getStatus()),
                () -> assertEquals(consumedAt, ticket.getConsumedAt()),
                () -> assertEquals(staffId, ticket.getConsumedByStaffId())
        );
    }

    @Test
    void consume_WhenTicketIsConsumed_ShouldThrowInvalidDrinkTicketStateException() {
        DrinkTicket ticket = DrinkTicket.pending(VolunteerID.generate(), DrinkType.BEER, Instant.now());
        ticket.consume("staff-123", Instant.now());

        assertThrows(
                InvalidDrinkTicketStateException.class,
                () -> ticket.consume("staff-456", Instant.now())
        );
    }

    @Test
    void consume_WhenTicketIsExpired_ShouldThrowInvalidDrinkTicketStateException() {
        DrinkTicket ticket = DrinkTicket.rehydrate(
                DrinkTicketID.generate(),
                VolunteerID.generate(),
                DrinkType.BEER,
                DrinkTicketStatus.EXPIRED,
                Instant.now().minusSeconds(1),
                Instant.now().minusSeconds(120),
                null,
                null
        );

        assertThrows(
                DrinkTicketExpiredException.class,
                () -> ticket.consume("staff-123", Instant.now())
        );
    }
}
