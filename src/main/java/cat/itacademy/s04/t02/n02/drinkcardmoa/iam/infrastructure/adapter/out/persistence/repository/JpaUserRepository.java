package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserJpaEntity, String>, JpaSpecificationExecutor<UserJpaEntity> {
    boolean existsByEmail(String email);
    Optional<UserJpaEntity> findByEmail(String email);
}
