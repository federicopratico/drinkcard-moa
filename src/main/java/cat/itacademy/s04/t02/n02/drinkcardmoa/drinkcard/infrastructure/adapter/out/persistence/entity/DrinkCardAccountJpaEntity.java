package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "volunteers")
@Getter
@Setter
@NoArgsConstructor
public class DrinkCardAccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_gen")
    @SequenceGenerator(
            name = "my_gen",
            sequenceName = "id_persistence_seq",
            allocationSize = 50
    )
    private Long id;

    @Column(name = "volunteer_id", nullable = false, unique = true)
    private String volunteerId;

    @Column(name = "credits", nullable = false)
    private int credits;

    @Column(name = "last_purchase", nullable = false)
    private Instant lastPurchaseTimestamp;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    private DrinkCardAccountJpaEntity(Long id, String volunteerId, int credits, Instant lastPurchaseTimestamp, Instant createdAt) {
        this.id = id;
        this.volunteerId = volunteerId;
        this.credits = credits;
        this.lastPurchaseTimestamp = lastPurchaseTimestamp;
        this.createdAt = createdAt;
    }

    public static DrinkCardAccountJpaEntity create(String volunteerId, int credits, Instant lastPurchaseTimestamp, Instant createdAt) {
        return new DrinkCardAccountJpaEntity(
                null,
                volunteerId,
                credits,
                lastPurchaseTimestamp,
                createdAt
        );
    }
}
