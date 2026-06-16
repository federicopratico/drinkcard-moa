package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidTokenException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenFamilyID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;

import java.time.Instant;

public class RefreshToken {
    private final RefreshTokenID id;
    private final VolunteerID userId;
    private final HashedToken tokenHash;
    private final RefreshTokenFamilyID familyId;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Instant lastUsedAt;
    private Instant revokedAt;
    private RefreshTokenID replacedBy;

    private RefreshToken(RefreshTokenID id, VolunteerID userId, HashedToken tokenHash, RefreshTokenFamilyID familyId, Instant createdAt, Instant expiresAt, Instant lastUsedAt, Instant revokedAt, RefreshTokenID replacedBy) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.familyId = familyId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.lastUsedAt = lastUsedAt;
        this.revokedAt = revokedAt;
        this.replacedBy = replacedBy;
    }

    public static RefreshToken create(
            RefreshTokenID id,
            VolunteerID userId,
            HashedToken tokenHash,
            RefreshTokenFamilyID familyId,
            Instant createdAt,
            Instant expiresAt
    ) {
        if(!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiresAt must be before createdAt");
        }

        return new RefreshToken(
                id,
                userId,
                tokenHash,
                familyId,
                createdAt,
                expiresAt,
                null,
                null,
                null
        );
    }

    public static RefreshToken rehydrate(RefreshTokenID id, VolunteerID userId, HashedToken tokenHash, RefreshTokenFamilyID familyId, Instant createdAt, Instant expiresAt, Instant lastUsedAt, Instant revokedAt, RefreshTokenID replacedBy) {
        return new RefreshToken(id, userId, tokenHash, familyId, createdAt, expiresAt, lastUsedAt, revokedAt, replacedBy);
    }

    public RefreshToken rotate(HashedToken newTokenHash, Instant now, Instant newExpiresAt) {
        if (!isUsable(now)) {
            throw new InvalidTokenException("Token is not usable");
        }

        RefreshToken next = RefreshToken.create(
                RefreshTokenID.generate(),
                userId,
                newTokenHash,
                familyId,
                now,
                newExpiresAt
        );

        this.lastUsedAt = now;
        this.revokedAt = now;
        this.replacedBy = next.getId();

        return next;
    }

    public void revoke(Instant now) {
        if (isRevoked()) {
            return;
        }

        this.revokedAt = now;
    }

    public boolean isUsable(Instant now) {
        return !isExpired(now) && !isRevoked();
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public boolean wasReplaced() {
        return replacedBy != null;
    }

    public RefreshTokenID getId() {
        return id;
    }
    public VolunteerID getUserId() {
        return userId;
    }
    public HashedToken getTokenHash() {
        return tokenHash;
    }
    public RefreshTokenFamilyID getFamilyId() {
        return familyId;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getExpiresAt() {
        return expiresAt;
    }
    public Instant getLastUsedAt() {
        return lastUsedAt;
    }
    public Instant getRevokedAt() {
        return revokedAt;
    }
    public RefreshTokenID getReplacedBy() {
        return replacedBy;
    }
}
