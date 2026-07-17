package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkConsumption;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.TopVolunteer;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkTicketJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaDrinkTicketRepository extends JpaRepository <DrinkTicketJpaEntity, UUID>,
        JpaSpecificationExecutor<DrinkTicketJpaEntity> {
    Optional<DrinkTicketJpaEntity> findByDrinkTicketId(UUID drinkTicketId);
    boolean existsByVolunteerIdAndStatusAndExpiresAtAfter(UUID volunteerId, String status, Instant now);
    List<DrinkTicketJpaEntity> findByStatusAndExpiresAtLessThanEqual(String status, Instant now);

    @Query("SELECT new cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.TopVolunteer(dt.volunteerId, COUNT(dt)) FROM DrinkTicketJpaEntity dt WHERE dt.status = :status GROUP BY dt.volunteerId ORDER BY COUNT(dt) DESC")
    Page<TopVolunteer> getTopByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT new cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkConsumption(dt.drinkType, COUNT(dt)) FROM DrinkTicketJpaEntity dt WHERE dt.status = 'CONSUMED' GROUP BY dt.drinkType ORDER BY COUNT(dt) DESC")
    List<DrinkConsumption> getConsumptionStats();

}
