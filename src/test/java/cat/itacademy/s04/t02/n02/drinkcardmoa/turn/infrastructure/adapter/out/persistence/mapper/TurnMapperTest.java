package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.aggregate.Turn;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.valueobject.TurnID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.out.persistence.entity.TurnJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TurnMapperTest {

    private final TurnMapper mapper = new TurnMapper();

    @Test
    void toEntity_ShouldMapAllFields() {
        TurnID turnId = TurnID.generate();
        Email email = Email.from("jane@example.com");
        LocalDate date = LocalDate.of(2026, 7, 17);
        Instant createdAt = Instant.parse("2026-05-18T10:00:00Z");
        Turn turn = Turn.rehydrate(turnId, email, date, createdAt);

        TurnJpaEntity entity = mapper.toEntity(turn);

        assertAll(
                () -> assertEquals(turnId.value(), entity.getTurnId()),
                () -> assertEquals(email.asString(), entity.getEmail()),
                () -> assertEquals(date, entity.getTurnDate()),
                () -> assertEquals(createdAt, entity.getCreatedAt())
        );
    }

    @Test
    void toDomain_ShouldMapAllFields() {
        TurnID turnId = TurnID.generate();
        LocalDate date = LocalDate.of(2026, 7, 17);
        Instant createdAt = Instant.parse("2026-05-18T10:00:00Z");
        TurnJpaEntity entity = TurnJpaEntity.create(
                turnId.value(),
                "jane@example.com",
                date,
                createdAt
        );

        Turn turn = mapper.toDomain(entity);

        assertAll(
                () -> assertEquals(turnId, turn.getTurnId()),
                () -> assertEquals("jane@example.com", turn.getEmail().asString()),
                () -> assertEquals(date, turn.getDate()),
                () -> assertEquals(createdAt, turn.getCreatedAt())
        );
    }
}
