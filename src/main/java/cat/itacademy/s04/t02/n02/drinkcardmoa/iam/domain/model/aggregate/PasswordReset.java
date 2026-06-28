package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.PasswordResetStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.PasswordResetID;
import lombok.Getter;

import java.time.Instant;

@Getter
public class PasswordReset {

    private final PasswordResetID passwordResetId;
    private final Email email;
    private final HashedToken token;
    private PasswordResetStatus status;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Instant usedAt;

    private PasswordReset(PasswordResetID passwordResetId, Email email, HashedToken token, PasswordResetStatus status, Instant createdAt, Instant expiresAt, Instant usedAt) {
        this.passwordResetId = passwordResetId;
        this.email = email;
        this.token = token;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }

    public static PasswordReset create(PasswordResetID passwordResetId, Email email, HashedToken token, Instant createdAt, Instant expiresAt) {
        return new PasswordReset(passwordResetId, email, token, PasswordResetStatus.PENDING, createdAt, expiresAt, null);
    }

    public static PasswordReset rehydrate(PasswordResetID passwordResetId, Email email, HashedToken token, PasswordResetStatus status, Instant createdAt, Instant expiresAt, Instant usedAt) {
        return new PasswordReset(passwordResetId, email, token, status, createdAt, expiresAt, usedAt);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isUsable(Instant now) {
        return !isExpired(now) && status == PasswordResetStatus.PENDING;
    }

    public void markAsUsed(Instant now) {
        this.status = PasswordResetStatus.USED;
        this.usedAt = now;
    }

    public void markAsExpired() {
        this.status = PasswordResetStatus.EXPIRED;
    }

    public void markAsRevoked() {
        this.status = PasswordResetStatus.REVOKED;
    }

}

