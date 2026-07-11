package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "turns")
@Getter
@Setter
@NoArgsConstructor
public class TurnJpaEntity {

    @Id
    @Column(name = "turn_id", nullable = false, updatable = false)
    private UUID turnId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "turn_date", nullable = false)
    private LocalDate turnDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    private TurnJpaEntity(UUID turnId, String email, LocalDate turnDate, Instant createdAt) {
        this.turnId = turnId;
        this.email = email;
        this.turnDate = turnDate;
        this.createdAt = createdAt;
    }

    public static TurnJpaEntity create(UUID turnId, String email, LocalDate turnDate, Instant createdAt) {
        return new TurnJpaEntity(turnId, email, turnDate, createdAt);
    }
}
