package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkCardAccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaDrinkCardAccountRepository extends JpaRepository <DrinkCardAccountJpaEntity, Long> {
    Optional<DrinkCardAccountJpaEntity> findByVolunteerId(String volunteerID);
    boolean existsByVolunteerId(String volunteerID);
}
