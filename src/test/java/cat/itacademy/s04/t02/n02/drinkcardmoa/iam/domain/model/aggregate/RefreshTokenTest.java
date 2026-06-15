package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidTokenException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenFamilyID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-06-14T10:00:00Z");

    private static final Instant EXPIRES_AT =
            Instant.parse("2026-06-21T10:00:00Z");

    @Test
    void create_ShouldCreateUsableRefreshToken() {
        RefreshTokenID tokenId = RefreshTokenID.generate();
        VolunteerID userId = VolunteerID.generate();
        HashedToken tokenHash = HashedToken.from("initial-token-hash");
        RefreshTokenFamilyID familyId = RefreshTokenFamilyID.generate();

        RefreshToken token = RefreshToken.create(
                tokenId,
                userId,
                tokenHash,
                familyId,
                CREATED_AT,
                EXPIRES_AT
        );

        assertAll(
                () -> assertEquals(tokenId, token.getId()),
                () -> assertEquals(userId, token.getUserId()),
                () -> assertEquals(tokenHash, token.getTokenHash()),
                () -> assertEquals(familyId, token.getFamilyId()),
                () -> assertEquals(CREATED_AT, token.getCreatedAt()),
                () -> assertEquals(EXPIRES_AT, token.getExpiresAt()),
                () -> assertNull(token.getLastUsedAt()),
                () -> assertNull(token.getRevokedAt()),
                () -> assertNull(token.getReplacedBy()),
                () -> assertFalse(token.isRevoked()),
                () -> assertFalse(token.wasReplaced()),
                () -> assertTrue(token.isUsable(CREATED_AT))
        );
    }

    @Test
    void create_WhenExpirationIsBeforeCreation_ShouldThrowException() {
        Instant invalidExpiration = CREATED_AT.minusSeconds(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> RefreshToken.create(
                        RefreshTokenID.generate(),
                        VolunteerID.generate(),
                        HashedToken.from("token-hash"),
                        RefreshTokenFamilyID.generate(),
                        CREATED_AT,
                        invalidExpiration
                )
        );
    }

    @Test
    void create_WhenExpirationEqualsCreation_ShouldThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RefreshToken.create(
                        RefreshTokenID.generate(),
                        VolunteerID.generate(),
                        HashedToken.from("token-hash"),
                        RefreshTokenFamilyID.generate(),
                        CREATED_AT,
                        CREATED_AT
                )
        );
    }

    @Test
    void isExpired_WhenNowIsBeforeExpiration_ShouldReturnFalse() {
        RefreshToken token = createToken();

        assertFalse(token.isExpired(EXPIRES_AT.minusSeconds(1)));
    }

    @Test
    void isExpired_WhenNowEqualsExpiration_ShouldReturnTrue() {
        RefreshToken token = createToken();

        assertTrue(token.isExpired(EXPIRES_AT));
    }

    @Test
    void isExpired_WhenNowIsAfterExpiration_ShouldReturnTrue() {
        RefreshToken token = createToken();

        assertTrue(token.isExpired(EXPIRES_AT.plusSeconds(1)));
    }

    @Test
    void rotate_WhenTokenIsUsable_ShouldReplaceCurrentToken() {
        RefreshToken currentToken = createToken();
        HashedToken newHash = HashedToken.from("new-token-hash");
        Instant rotationTime = CREATED_AT.plusSeconds(60);
        Instant newExpiration = EXPIRES_AT.plusSeconds(60);

        RefreshToken nextToken = currentToken.rotate(
                newHash,
                rotationTime,
                newExpiration
        );

        assertAll(
                () -> assertTrue(currentToken.isRevoked()),
                () -> assertTrue(currentToken.wasReplaced()),
                () -> assertEquals(rotationTime, currentToken.getLastUsedAt()),
                () -> assertEquals(rotationTime, currentToken.getRevokedAt()),
                () -> assertEquals(nextToken.getId(), currentToken.getReplacedBy()),

                () -> assertNotEquals(currentToken.getId(), nextToken.getId()),
                () -> assertEquals(currentToken.getUserId(), nextToken.getUserId()),
                () -> assertEquals(currentToken.getFamilyId(), nextToken.getFamilyId()),
                () -> assertEquals(newHash, nextToken.getTokenHash()),
                () -> assertEquals(rotationTime, nextToken.getCreatedAt()),
                () -> assertEquals(newExpiration, nextToken.getExpiresAt()),
                () -> assertTrue(nextToken.isUsable(rotationTime)),
                () -> assertFalse(nextToken.isRevoked()),
                () -> assertFalse(nextToken.wasReplaced())
        );
    }

    @Test
    void rotate_WhenTokenIsExpired_ShouldThrowInvalidTokenException() {
        RefreshToken token = createToken();

        assertThrows(
                InvalidTokenException.class,
                () -> token.rotate(
                        HashedToken.from("new-token-hash"),
                        EXPIRES_AT,
                        EXPIRES_AT.plusSeconds(60)
                )
        );
    }

    @Test
    void rotate_WhenTokenIsRevoked_ShouldThrowInvalidTokenException() {
        RefreshToken token = createToken();
        Instant revokedAt = CREATED_AT.plusSeconds(60);

        token.revoke(revokedAt);

        assertThrows(
                InvalidTokenException.class,
                () -> token.rotate(
                        HashedToken.from("new-token-hash"),
                        revokedAt.plusSeconds(1),
                        EXPIRES_AT.plusSeconds(60)
                )
        );
    }

    @Test
    void revoke_WhenTokenIsActive_ShouldRevokeToken() {
        RefreshToken token = createToken();
        Instant revokedAt = CREATED_AT.plusSeconds(60);

        token.revoke(revokedAt);

        assertAll(
                () -> assertTrue(token.isRevoked()),
                () -> assertFalse(token.isUsable(revokedAt)),
                () -> assertEquals(revokedAt, token.getRevokedAt()),
                () -> assertNull(token.getLastUsedAt()),
                () -> assertFalse(token.wasReplaced())
        );
    }

    @Test
    void revoke_WhenTokenIsAlreadyRevoked_ShouldPreserveOriginalTimestamp() {
        RefreshToken token = createToken();
        Instant firstRevocation = CREATED_AT.plusSeconds(60);
        Instant secondRevocation = CREATED_AT.plusSeconds(120);

        token.revoke(firstRevocation);
        token.revoke(secondRevocation);

        assertEquals(firstRevocation, token.getRevokedAt());
    }

    @Test
    void rehydrate_ShouldRestoreAllPersistedValues() {
        RefreshTokenID tokenId = RefreshTokenID.generate();
        RefreshTokenID replacementId = RefreshTokenID.generate();
        VolunteerID userId = VolunteerID.generate();
        HashedToken tokenHash = HashedToken.from("persisted-token-hash");
        RefreshTokenFamilyID familyId = RefreshTokenFamilyID.generate();
        Instant lastUsedAt = CREATED_AT.plusSeconds(60);
        Instant revokedAt = lastUsedAt;

        RefreshToken token = RefreshToken.rehydrate(
                tokenId,
                userId,
                tokenHash,
                familyId,
                CREATED_AT,
                EXPIRES_AT,
                lastUsedAt,
                revokedAt,
                replacementId
        );

        assertAll(
                () -> assertEquals(tokenId, token.getId()),
                () -> assertEquals(userId, token.getUserId()),
                () -> assertEquals(tokenHash, token.getTokenHash()),
                () -> assertEquals(familyId, token.getFamilyId()),
                () -> assertEquals(CREATED_AT, token.getCreatedAt()),
                () -> assertEquals(EXPIRES_AT, token.getExpiresAt()),
                () -> assertEquals(lastUsedAt, token.getLastUsedAt()),
                () -> assertEquals(revokedAt, token.getRevokedAt()),
                () -> assertEquals(replacementId, token.getReplacedBy()),
                () -> assertTrue(token.isRevoked()),
                () -> assertTrue(token.wasReplaced())
        );
    }

    private RefreshToken createToken() {
        return RefreshToken.create(
                RefreshTokenID.generate(),
                VolunteerID.generate(),
                HashedToken.from("initial-token-hash"),
                RefreshTokenFamilyID.generate(),
                CREATED_AT,
                EXPIRES_AT
        );
    }
}