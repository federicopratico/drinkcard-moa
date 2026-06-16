package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor
public class RefreshTokenJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token_id")
    private UUID replacedBy;

    private RefreshTokenJpaEntity(UUID id, String userId, String tokenHash, UUID familyId, Instant createdAt, Instant expiresAt, Instant lastUsedAt, Instant revokedAt, UUID replacedBy) {
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

    public static RefreshTokenJpaEntity create(UUID id, String userId, String tokenHash, UUID familyId, Instant createdAt, Instant expiresAt, Instant lastUsedAt, Instant revokedAt, UUID replacedBy) {
        return new RefreshTokenJpaEntity(id, userId, tokenHash, familyId, createdAt, expiresAt, lastUsedAt, revokedAt, replacedBy);
    }
}
