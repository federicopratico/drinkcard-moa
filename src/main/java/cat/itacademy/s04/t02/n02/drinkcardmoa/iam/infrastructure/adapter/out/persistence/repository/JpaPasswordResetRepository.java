package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.PasswordResetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaPasswordResetRepository extends JpaRepository<PasswordResetJpaEntity, UUID> {
    Optional<PasswordResetJpaEntity> findByPasswordResetToken(String token);

    @Modifying
    @Query("""
            UPDATE PasswordResetJpaEntity passwordReset
            SET passwordReset.status = 'REVOKED'
            WHERE passwordReset.email = :email
            AND passwordReset.status = 'PENDING'
            AND passwordReset.id <> :currentPasswordResetId
            """)
    void revokePendingByEmailExceptCurrent(
            @Param("email") String email,
            @Param("currentPasswordResetId") UUID currentPasswordResetId
    );
}
