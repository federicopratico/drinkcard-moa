package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkTicketJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaDrinkTicketRepository extends JpaRepository <DrinkTicketJpaEntity, UUID>,
        JpaSpecificationExecutor<DrinkTicketJpaEntity> {
    Optional<DrinkTicketJpaEntity> findByDrinkTicketId(UUID drinkTicketId);
    boolean existsByVolunteerIdAndStatusAndExpiresAtAfter(UUID volunteerId, String status, Instant now);
    List<DrinkTicketJpaEntity> findByStatusAndExpiresAtLessThanEqual(String status, Instant now);
}
