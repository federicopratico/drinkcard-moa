package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.InvitationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface JpaInvitationRepository extends JpaRepository<InvitationJpaEntity, String>, JpaSpecificationExecutor<InvitationJpaEntity> {
    Optional<InvitationJpaEntity> findByEmail(String email);
    Optional<InvitationJpaEntity> findByInvitationToken(String string);
}
