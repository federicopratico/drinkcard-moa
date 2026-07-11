package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.out.persistence.repository;

import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.out.persistence.entity.TurnJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.UUID;

public interface JpaTurnRepository extends JpaRepository<TurnJpaEntity, UUID>,
        JpaSpecificationExecutor<TurnJpaEntity> {
    boolean existsByEmailAndTurnDate(String email, LocalDate turnDate);
}
