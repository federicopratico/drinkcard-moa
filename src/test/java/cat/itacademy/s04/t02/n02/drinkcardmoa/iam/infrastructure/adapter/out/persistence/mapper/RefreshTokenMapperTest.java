package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.RefreshToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenFamilyID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenMapperTest {

    private final RefreshTokenMapper mapper = new RefreshTokenMapper();

    @Test
    void toDomain_ShouldMapEntityToDomain() {
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(3600);

        RefreshTokenJpaEntity entity = RefreshTokenJpaEntity.create(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                "tokenHash",
                UUID.randomUUID(),
                createdAt,
                expiresAt,
                null,
                null,
                null
        );

        RefreshToken token = mapper.toDomain(entity);

        assertAll(
                () -> assertEquals(entity.getId(), token.getId().value()),
                () -> assertEquals(entity.getUserId(), token.getUserId().asString()),
                () -> assertEquals(entity.getTokenHash(), token.getTokenHash().asString()),
                () -> assertEquals(entity.getFamilyId(), token.getFamilyId().value()),
                () -> assertEquals(entity.getCreatedAt(), token.getCreatedAt()),
                () -> assertEquals(entity.getExpiresAt(), token.getExpiresAt()),
                () -> assertNull(token.getRevokedAt()),
                () -> assertNull(token.getLastUsedAt()),
                () -> assertNull(token.getReplacedBy())
        );
    }

    @Test
    void toEntity_shouldMapDomainToEntity() {
        RefreshToken token = RefreshToken.create(
                RefreshTokenID.generate(),
                VolunteerID.generate(),
                HashedToken.from("hashedToken"),
                RefreshTokenFamilyID.generate(),
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        RefreshTokenJpaEntity entity = mapper.toEntity(token);

        assertAll(
                () -> assertEquals(token.getId().value(), entity.getId()),
                () -> assertEquals(token.getUserId().asString(), entity.getUserId()),
                () -> assertEquals(token.getTokenHash().asString(), entity.getTokenHash()),
                () -> assertEquals(token.getFamilyId().value(), entity.getFamilyId()),
                () -> assertEquals(token.getCreatedAt(), entity.getCreatedAt()),
                () -> assertEquals(token.getExpiresAt(), entity.getExpiresAt()),
                () -> assertNull(entity.getLastUsedAt()),
                () -> assertNull(entity.getRevokedAt()),
                () -> assertNull(entity.getReplacedBy())
        );
    }

    @Test
    void toDomain_WhenOptionalFieldsExist_ShouldMapThem() {
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(3600);
        Instant lastUsedAt = createdAt.plusSeconds(60);
        Instant revokedAt = lastUsedAt;
        UUID replacedBy = UUID.randomUUID();

        RefreshTokenJpaEntity entity = RefreshTokenJpaEntity.create(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                "tokenHash",
                UUID.randomUUID(),
                createdAt,
                expiresAt,
                lastUsedAt,
                revokedAt,
                replacedBy
        );

        RefreshToken token = mapper.toDomain(entity);

        assertAll(
                () -> assertEquals(lastUsedAt, token.getLastUsedAt()),
                () -> assertEquals(revokedAt, token.getRevokedAt()),
                () -> assertNotNull(token.getReplacedBy()),
                () -> assertEquals(replacedBy, token.getReplacedBy().value()),
                () -> assertTrue(token.isRevoked()),
                () -> assertTrue(token.wasReplaced())
        );
    }

    @Test
    void toEntity_WhenOptionalFieldsExist_ShouldMapThem() {
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(3600);
        Instant lastUsedAt = createdAt.plusSeconds(60);
        Instant revokedAt = lastUsedAt;
        RefreshTokenID replacedBy = RefreshTokenID.generate();

        RefreshToken token = RefreshToken.rehydrate(
                RefreshTokenID.generate(),
                VolunteerID.generate(),
                HashedToken.from("hashedToken"),
                RefreshTokenFamilyID.generate(),
                createdAt,
                expiresAt,
                lastUsedAt,
                revokedAt,
                replacedBy
        );

        RefreshTokenJpaEntity entity = mapper.toEntity(token);

        assertAll(
                () -> assertEquals(lastUsedAt, entity.getLastUsedAt()),
                () -> assertEquals(revokedAt, entity.getRevokedAt()),
                () -> assertEquals(replacedBy.value(), entity.getReplacedBy())
        );
    }
}