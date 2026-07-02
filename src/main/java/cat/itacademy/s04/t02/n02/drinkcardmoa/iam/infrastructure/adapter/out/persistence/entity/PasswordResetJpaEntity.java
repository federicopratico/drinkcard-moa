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

@Entity
@Table(name = "password_reset")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetJpaEntity {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false, name = "email")
    private String email;

    @Column(unique = true, nullable = false, name = "password_reset_token")
    private String passwordResetToken;

    @Column(nullable = false, name = "status")
    private String status;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    private PasswordResetJpaEntity(UUID id, String email, String passwordResetToken, String status, Instant createdAt, Instant expiresAt, Instant usedAt) {
        this.id = id;
        this.email = email;
        this.passwordResetToken = passwordResetToken;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }

    public static PasswordResetJpaEntity create(UUID id, String email, String passwordResetToken, String status, Instant createdAt, Instant expiresAt, Instant usedAt) {
        return new PasswordResetJpaEntity(id, email, passwordResetToken, status, createdAt, expiresAt, usedAt);
    }
}
