package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserJpaEntity, String> {
    boolean existsByEmail(String email);
    Optional<UserJpaEntity> findByEmail(String email);

    @Query("""
            SELECT u
            FROM UserJpaEntity u
            WHERE (:role IS NULL OR u.role = :role)\s
              AND (:status IS NULL OR u.status = :status)
              AND (:email IS NULL OR u.email = :email)
           \s""")
    List<UserJpaEntity> findAllByFilters(String role, String status, String email);
}
