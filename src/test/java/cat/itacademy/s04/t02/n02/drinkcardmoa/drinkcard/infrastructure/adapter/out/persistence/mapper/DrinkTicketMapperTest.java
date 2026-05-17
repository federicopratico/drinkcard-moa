package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicketID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicketStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkType;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkTicketJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DrinkTicketMapperTest {

    private final DrinkTicketMapper mapper = new DrinkTicketMapper();

    @Test
    void toDomain_ShouldMapEntityToDomain() {
        DrinkTicketID drinkTicketId = DrinkTicketID.generate();
        VolunteerID volunteerId = VolunteerID.generate();
        Instant createdAt = Instant.now().minusSeconds(120);
        Instant expiresAt = createdAt.plusSeconds(90);
        Instant consumedAt = createdAt.plusSeconds(30);
        String consumedByStaffId = "staff-123";

        DrinkTicketJpaEntity entity = DrinkTicketJpaEntity.create(
                drinkTicketId.value(),
                volunteerId.value(),
                "BEER",
                "CONSUMED",
                createdAt,
                expiresAt,
                consumedAt,
                consumedByStaffId
        );

        DrinkTicket drinkTicket = mapper.toDomain(entity);

        assertAll(
                () -> assertEquals(drinkTicketId, drinkTicket.getDrinkTicketId()),
                () -> assertEquals(volunteerId, drinkTicket.getVolunteerId()),
                () -> assertEquals(DrinkType.BEER, drinkTicket.getDrinkType()),
                () -> assertEquals(DrinkTicketStatus.CONSUMED, drinkTicket.getStatus()),
                () -> assertEquals(createdAt, drinkTicket.getCreatedAt()),
                () -> assertEquals(expiresAt, drinkTicket.getExpiresAt()),
                () -> assertEquals(consumedAt, drinkTicket.getConsumedAt()),
                () -> assertEquals(consumedByStaffId, drinkTicket.getConsumedByStaffId())
        );
    }

    @Test
    void toDomain_WhenEntityUsesLowercaseEnumValues_ShouldMapEntityToDomain() {
        DrinkTicketID drinkTicketId = DrinkTicketID.generate();
        VolunteerID volunteerId = VolunteerID.generate();
        Instant createdAt = Instant.now().minusSeconds(120);
        Instant expiresAt = createdAt.plusSeconds(90);

        DrinkTicketJpaEntity entity = DrinkTicketJpaEntity.create(
                drinkTicketId.value(),
                volunteerId.value(),
                "water",
                "pending",
                createdAt,
                expiresAt,
                null,
                null
        );

        DrinkTicket drinkTicket = mapper.toDomain(entity);

        assertAll(
                () -> assertEquals(drinkTicketId, drinkTicket.getDrinkTicketId()),
                () -> assertEquals(volunteerId, drinkTicket.getVolunteerId()),
                () -> assertEquals(DrinkType.WATER, drinkTicket.getDrinkType()),
                () -> assertEquals(DrinkTicketStatus.PENDING, drinkTicket.getStatus()),
                () -> assertEquals(createdAt, drinkTicket.getCreatedAt()),
                () -> assertEquals(expiresAt, drinkTicket.getExpiresAt()),
                () -> assertEquals(null, drinkTicket.getConsumedAt()),
                () -> assertEquals(null, drinkTicket.getConsumedByStaffId())
        );
    }

    @Test
    void toEntity_ShouldMapDomainToEntity() {
        DrinkTicketID drinkTicketId = DrinkTicketID.generate();
        VolunteerID volunteerId = VolunteerID.generate();
        Instant createdAt = Instant.now().minusSeconds(120);
        Instant expiresAt = createdAt.plusSeconds(90);
        Instant consumedAt = createdAt.plusSeconds(30);
        String consumedByStaffId = "staff-123";

        DrinkTicket drinkTicket = DrinkTicket.rehydrate(
                drinkTicketId,
                volunteerId,
                DrinkType.SOFT_DRINK,
                DrinkTicketStatus.CONSUMED,
                createdAt,
                expiresAt,
                consumedAt,
                consumedByStaffId
        );

        DrinkTicketJpaEntity entity = mapper.toEntity(drinkTicket);

        assertAll(
                () -> assertEquals(drinkTicketId.value(), entity.getDrinkTicketId()),
                () -> assertEquals(volunteerId.value(), entity.getVolunteerId()),
                () -> assertEquals("SOFT_DRINK", entity.getDrinkType()),
                () -> assertEquals("CONSUMED", entity.getStatus()),
                () -> assertEquals(createdAt, entity.getCreatedAt()),
                () -> assertEquals(expiresAt, entity.getExpiresAt()),
                () -> assertEquals(consumedAt, entity.getConsumedAt()),
                () -> assertEquals(consumedByStaffId, entity.getConsumedByStaffId())
        );
    }

    @Test
    void toEntity_WhenTicketIsPending_ShouldMapNullableConsumptionFields() {
        DrinkTicketID drinkTicketId = DrinkTicketID.generate();
        VolunteerID volunteerId = VolunteerID.generate();
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(90);

        DrinkTicket drinkTicket = DrinkTicket.rehydrate(
                drinkTicketId,
                volunteerId,
                DrinkType.BEER,
                DrinkTicketStatus.PENDING,
                createdAt,
                expiresAt,
                null,
                null
        );

        DrinkTicketJpaEntity entity = mapper.toEntity(drinkTicket);

        assertAll(
                () -> assertEquals(drinkTicketId.value(), entity.getDrinkTicketId()),
                () -> assertEquals(volunteerId.value(), entity.getVolunteerId()),
                () -> assertEquals("BEER", entity.getDrinkType()),
                () -> assertEquals("PENDING", entity.getStatus()),
                () -> assertEquals(createdAt, entity.getCreatedAt()),
                () -> assertEquals(expiresAt, entity.getExpiresAt()),
                () -> assertNull(entity.getConsumedAt()),
                () -> assertNull(entity.getConsumedByStaffId())
        );
    }
}
