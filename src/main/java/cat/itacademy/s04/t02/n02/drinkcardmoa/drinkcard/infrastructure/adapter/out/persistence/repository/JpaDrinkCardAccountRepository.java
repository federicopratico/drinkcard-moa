package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkCardAccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JpaDrinkCardAccountRepository extends JpaRepository<DrinkCardAccountJpaEntity, Long>,
        JpaSpecificationExecutor<DrinkCardAccountJpaEntity> {
    Optional<DrinkCardAccountJpaEntity> findByVolunteerId(String volunteerID);
    boolean existsByVolunteerId(String volunteerID);

    @Query("SELECT COALESCE(SUM(a.credits), 0) FROM DrinkCardAccountJpaEntity a")
    long sumAvailableCredits();

    long countByStatus(String status);
}
