package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkTicketJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaDrinkTicketRepository extends JpaRepository <DrinkTicketJpaEntity, UUID> {
    Optional<DrinkTicketJpaEntity> findByDrinkTicketId(UUID drinkTicketId);
}
