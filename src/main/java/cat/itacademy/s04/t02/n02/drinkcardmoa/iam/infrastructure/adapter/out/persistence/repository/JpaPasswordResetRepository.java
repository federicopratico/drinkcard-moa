package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.PasswordResetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaPasswordResetRepository extends JpaRepository<PasswordResetJpaEntity, UUID> {
    Optional<PasswordResetJpaEntity> findByToken(String token);
}
