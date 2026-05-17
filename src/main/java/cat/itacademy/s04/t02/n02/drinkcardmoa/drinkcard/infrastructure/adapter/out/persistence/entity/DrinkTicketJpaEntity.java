package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity;

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
@Table(name = "drink_tickets")
@Getter
@Setter
@NoArgsConstructor
public class DrinkTicketJpaEntity {

    @Id
    @Column(name = "drink_ticket_id", nullable = false)
    private UUID drinkTicketId;

    @Column(name = "volunteer_id", nullable = false)
    private UUID volunteerId;

    @Column(name = "drink_type", nullable = false)
    private String drinkType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "consumed_by_staff_id")
    private String consumedByStaffId;

    private DrinkTicketJpaEntity(UUID drinkTicketId, UUID volunteerId, String drinkType, String status, Instant createdAt, Instant expiresAt, Instant consumedAt, String consumedByStaffId) {
        this.drinkTicketId = drinkTicketId;
        this.volunteerId = volunteerId;
        this.drinkType = drinkType;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.consumedAt = consumedAt;
        this.consumedByStaffId = consumedByStaffId;
    }

    public static DrinkTicketJpaEntity create(UUID ticketId, UUID volunteerId, String drinkType, String status, Instant createdAt, Instant expiresAt, Instant consumedAt, String consumedByStaffId) {
        return new DrinkTicketJpaEntity(ticketId, volunteerId, drinkType, status, createdAt, expiresAt, consumedAt, consumedByStaffId);
    }
}
