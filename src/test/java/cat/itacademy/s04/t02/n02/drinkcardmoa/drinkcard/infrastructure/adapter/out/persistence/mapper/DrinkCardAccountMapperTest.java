package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccountStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkCardAccountJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DrinkCardAccountMapperTest {

    private final DrinkCardAccountMapper mapper = new DrinkCardAccountMapper();

    @Test
    void toEntity_ShouldMapDrinkCardAccountStatus() {
        VolunteerID volunteerId = VolunteerID.generate();
        Instant lastPurchaseTimestamp = Instant.now();
        Instant createdAt = Instant.now();
        DrinkCardAccount account = DrinkCardAccount.rehydrate(
                1L,
                volunteerId,
                10,
                lastPurchaseTimestamp,
                createdAt,
                DrinkCardAccountStatus.SUSPENDED
        );

        DrinkCardAccountJpaEntity entity = mapper.toEntity(account);

        assertAll(
                () -> assertEquals(1L, entity.getId()),
                () -> assertEquals(volunteerId.asString(), entity.getVolunteerId()),
                () -> assertEquals(10, entity.getCredits()),
                () -> assertEquals(lastPurchaseTimestamp, entity.getLastPurchaseTimestamp()),
                () -> assertEquals(createdAt, entity.getCreatedAt()),
                () -> assertEquals(DrinkCardAccountStatus.SUSPENDED.name(), entity.getStatus())
        );
    }

    @Test
    void toDomain_ShouldMapDrinkCardAccountStatus() {
        VolunteerID volunteerId = VolunteerID.generate();
        Instant lastPurchaseTimestamp = Instant.now();
        Instant createdAt = Instant.now();
        DrinkCardAccountJpaEntity entity = DrinkCardAccountJpaEntity.create(
                volunteerId.asString(),
                10,
                lastPurchaseTimestamp,
                createdAt,
                DrinkCardAccountStatus.SUSPENDED.name()
        );
        entity.setId(1L);

        DrinkCardAccount account = mapper.toDomain(entity);

        assertAll(
                () -> assertEquals(1L, account.getId()),
                () -> assertEquals(volunteerId, account.getVolunteerId()),
                () -> assertEquals(10, account.getCredits()),
                () -> assertEquals(lastPurchaseTimestamp, account.getLastPurchaseTimestamp()),
                () -> assertEquals(createdAt, account.getCreatedAt()),
                () -> assertEquals(DrinkCardAccountStatus.SUSPENDED, account.getStatus())
        );
    }
}
